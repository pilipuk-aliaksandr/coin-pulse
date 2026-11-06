package by.pilipuk.coinpulse.controller;

import by.pilipuk.api.PricesApi;
import by.pilipuk.coinpulse.service.PriceService;
import by.pilipuk.dto.AveragePriceDto;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Реализация сгенерированного Open API Generator интерфейса PricesApi
 * Спецификация для генератора находится в resources/specification/spec.yaml).
 */
@RestController
@RequiredArgsConstructor
public class PriceController implements PricesApi {

    private final PriceService priceService;

    @Override
    public PriceDto getLatestPrice(String symbol) {
        return priceService.getLatestPrice(symbol);
    }

    @Override
    public AveragePriceDto getAveragePrice(OffsetDateTime from, OffsetDateTime to, String symbol) {
        return priceService.getAveragePrice(from.toInstant(), to.toInstant(), symbol);
    }

    @Override
    public PricePageResponseDto getPricesPage(
            OffsetDateTime from, OffsetDateTime to, String symbol, Integer page, Integer size
    ) {
        return priceService.getPrices(from.toInstant(), to.toInstant(), symbol, page, size);
    }

    @Override
    public TrendResponseDto getPriceTrend(OffsetDateTime from, OffsetDateTime to, String symbol) {
        return priceService.getTrend(from.toInstant(), to.toInstant(), symbol);
    }
}
