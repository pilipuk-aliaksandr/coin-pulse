package by.pilipuk.coinpulse.service;

import by.pilipuk.coinpulse.exception.ApplicationException;
import by.pilipuk.coinpulse.mapper.PriceMapper;
import by.pilipuk.coinpulse.model.enums.Trend;
import by.pilipuk.coinpulse.repository.PriceRepository;
import by.pilipuk.dto.AveragePriceDto;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static by.pilipuk.coinpulse.exception.ApplicationExceptionCode.INVALID_PERIOD;
import static by.pilipuk.coinpulse.exception.ApplicationExceptionCode.PRICE_NOT_FOUND;
import static by.pilipuk.coinpulse.exception.ApplicationExceptionCode.PRICE_NOT_FOUND_IN_PERIOD;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceService {

    private static final int PRICE_SCALE = 8;

    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;
    private final PriceValidator priceValidator;

    public PriceDto getLatestPrice(String symbol) {
        var price = priceRepository.findFirstBySymbolOrderByFetchedAtDesc(symbol)
                .orElseThrow(() -> ApplicationException.create(PRICE_NOT_FOUND, symbol));

        return priceMapper.toPriceDto(price);
    }

    public AveragePriceDto getAveragePrice(Instant from, Instant to, String symbol) {
        if (priceValidator.isNotValidPeriod(from, to, Instant.now())) {
            throw createInvalidPeriodException(from, to);
        }
        var avgPrice = priceRepository.findAveragePrice(symbol, toUtc(from), toUtc(to))
                .map(average -> average.setScale(PRICE_SCALE, RoundingMode.HALF_UP))
                .orElseThrow(() -> createPriceNotFoundInPeriodException(symbol, from, to));

        return priceMapper.toAveragePriceDto(avgPrice);
    }

    public PricePageResponseDto getPrices(Instant from, Instant to, String symbol, int page, int size) {
        if (priceValidator.isNotValidPeriod(from, to, Instant.now())) {
            throw createInvalidPeriodException(from, to);
        }
        var pageable = PageRequest.of(page, size, Sort.by("fetchedAt", "id"));
        var pagePrice = priceRepository
                .findBySymbolAndFetchedAtBetween(symbol, toUtc(from), toUtc(to), pageable);

        return priceMapper.toPageDto(pagePrice);
    }

    public TrendResponseDto getTrend(Instant from, Instant to, String symbol) {
        if (priceValidator.isNotValidPeriod(from, to, Instant.now())) {
            throw createInvalidPeriodException(from, to);
        }

        var firstPrice = priceRepository
                .findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(symbol, toUtc(from), toUtc(to))
                .orElseThrow(() -> createPriceNotFoundInPeriodException(symbol, from, to));

        var lastPrice = priceRepository
                .findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(symbol, toUtc(from), toUtc(to))
                .orElseThrow(() -> createPriceNotFoundInPeriodException(symbol, from, to));

        int comparison = lastPrice.getPrice().compareTo(firstPrice.getPrice());

        var trend = comparison > 0 ? Trend.UPWARD
                : comparison < 0 ? Trend.DOWNWARD
                : Trend.FLAT;

        return priceMapper.toTrendDto(symbol, from, to, trend);
    }

    private LocalDateTime toUtc(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private ApplicationException createPriceNotFoundInPeriodException(String symbol, Instant from, Instant to) {
        return ApplicationException.create(PRICE_NOT_FOUND_IN_PERIOD, Map.of(
                "symbol", symbol,
                "from", from.toString(),
                "to", to.toString()));
    }

    private ApplicationException createInvalidPeriodException(Instant from, Instant to) {
        return ApplicationException.create(INVALID_PERIOD, Map.of(
                "from", String.valueOf(from),
                "to", String.valueOf(to)));
    }
}
