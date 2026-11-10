package by.pilipuk.coinpulse.client.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Настройки интеграции с Binance (префикс {@code binance} в application.yaml).
 *
 * @param baseUrl          базовый URL Binance API
 * @param symbols          торговые пары, которые опрашиваем
 * @param pollInterval     пауза между опросами, ISO-8601 (например, PT30S)
 * @param connectTimeout   тайм-аут установки соединения
 * @param readTimeout      тайм-аут ожидания ответа
 * @param rateLimitBackoff пауза при 429/418, если Binance не прислал заголовок Retry-After
 */
@Validated
@ConfigurationProperties(prefix = "binance")
public record BinanceProperties(
        @NotBlank String baseUrl,
        @NotEmpty @NotNull List<String> symbols,
        @NotNull Duration pollInterval,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout,
        @NotNull Duration rateLimitBackoff
) {
    public BinanceProperties {
        symbols = symbols == null ? List.of() : List.copyOf(symbols);
    }

    @Override
    public List<String> symbols() {
        return List.copyOf(symbols);
    }
}
