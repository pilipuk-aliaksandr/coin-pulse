package by.pilipuk.coinpulse.scheduler;

import by.pilipuk.coinpulse.client.exception.BinanceIntegrationException;
import by.pilipuk.coinpulse.client.property.BinanceProperties;
import by.pilipuk.coinpulse.service.PriceCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationException.RETRY_AFTER_SECONDS;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_RATE_LIMIT;

/**
 * Периодический опрос Binance (интервал задаётся параметром BINANCE_POLL_INTERVAL в env-переменных).
 * Сбой одного цикла не останавливает расписание. При RATE_LIMIT ошибке со стороны Binance пропускаем попытки на
 * полученное из header-а Retry-After или на установленное в application.yaml значение rateLimitBackoff.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "binance.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PricePollingScheduler {

    private final PriceCollectorService collector;
    private final BinanceProperties binanceProperties;

    private Instant pausedUntil = Instant.MIN;

    @Scheduled(fixedDelayString = "${binance.poll-interval}")
    public void poll() {
        if (Instant.now().isBefore(pausedUntil)) {
            log.warn("Polling skipped: Binance rate limit, paused until {}", pausedUntil);
            return;
        }
        for (String symbol : binanceProperties.symbols()) {
            if (!collectSafely(symbol)) {
                return;
            }
        }
    }

    private boolean collectSafely(String symbol) {
        try {
            collector.collect(symbol);
            return true;
        } catch (BinanceIntegrationException e) {
            if (e.hasCode(BINANCE_RATE_LIMIT)) {
                long retryAfterSeconds = ((Number) Objects.requireNonNull(
                        e.getParameter(RETRY_AFTER_SECONDS), "retryAfterSeconds is null")).longValue();

                pausedUntil = Instant.now().plusSeconds(retryAfterSeconds);

                log.error("{}. Polling paused until {}", e.getMessage(), pausedUntil);
                return false;
            }
            log.atLevel(e.getLogLevel()).log("Price collection skipped for {}: {}", symbol, e.getMessage());
            return true;
        } catch (RuntimeException e) {
            log.error("Unexpected error during price collection for {}", symbol, e);
            return true;
        }
    }
}
