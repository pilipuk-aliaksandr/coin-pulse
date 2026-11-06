package by.pilipuk.coinpulse.environment;

import by.pilipuk.coinpulse.model.entity.Price;
import by.pilipuk.dto.AveragePriceDto;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class PriceTestFactory {

    private PriceTestFactory() {
    }

    public static Price createBnbUsdtPrice(String price, LocalDateTime fetchedAt) {
        return Price.builder()
                .symbol("BNBUSDT")
                .price(new BigDecimal(price))
                .fetchedAt(fetchedAt)
                .createdAt(fetchedAt)
                .build();
    }

    public static PriceDto createPriceDto(String symbol, String price, OffsetDateTime fetchedAt) {

        return PriceDto.builder()
                .symbol(symbol)
                .price(price)
                .fetchedAt(fetchedAt)
                .build();
    }

    public static AveragePriceDto createAveragePriceDto(String averagePrice) {

        return AveragePriceDto.builder()
                .averagePrice(averagePrice)
                .build();
    }

    public static PricePageResponseDto createPricePageResponseDto(
            List<PriceDto> items, int page, int size, long totalElements, boolean hasNext
    ) {
        return PricePageResponseDto.builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .hasNext(hasNext)
                .build();
    }

    public static TrendResponseDto createTrendResponseDto(
            String symbol, OffsetDateTime startDate, OffsetDateTime endDate, TrendResponseDto.TrendEnum trend
    ) {
        return TrendResponseDto.builder()
                .symbol(symbol)
                .startDate(startDate)
                .endDate(endDate)
                .trend(trend)
                .build();
    }

    public static OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        return dateTime.atOffset(ZoneOffset.UTC);
    }
}
