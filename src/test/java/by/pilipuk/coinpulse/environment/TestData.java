package by.pilipuk.coinpulse.environment;

import by.pilipuk.coinpulse.model.entity.PriceEntity;
import by.pilipuk.dto.AveragePriceDto;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestData {

    public static final String FIRST_TEST_PRICE = "50000.00000000";
    public static final String SECOND_TEST_PRICE = "60000.00000000";
    public static final String THIRD_TEST_PRICE = "70000.00000000";
    public static final String FIRST_TEST_DATETIME = "2026-09-01T00:00:00Z";
    public static final String SECOND_TEST_DATETIME = "2026-09-30T23:59:59Z";
    public static final String SYMBOL = "BNBUSDT";
    public static final String SYMBOL_LITERAL = "symbol";
    public static final String FROM_LITERAL = "from";
    public static final String PAGE_LITERAL = "page";
    public static final String SIZE_LITERAL = "size";
    public static final String TO_LITERAL = "to";
    public static final String NONEXIST_LITERAL = "NONEXIST";
    public static final String CONTENT_TYPE_LITERAL = "Content-Type";
    public static final String CONTENT_TYPE = "application/json";

    private TestData() {
    }

    public static PriceEntity createBnbUsdtPrice(String price, LocalDateTime fetchedAt) {
        return PriceEntity.builder()
                .symbol(SYMBOL)
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
