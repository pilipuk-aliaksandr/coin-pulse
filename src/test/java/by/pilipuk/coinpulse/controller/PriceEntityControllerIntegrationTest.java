
package by.pilipuk.coinpulse.controller;

import by.pilipuk.coinpulse.controller.base.BaseControllerTest;
import by.pilipuk.coinpulse.environment.PostgreSqlTestContainer;
import by.pilipuk.coinpulse.environment.TestData;
import by.pilipuk.coinpulse.environment.TruncateDbTablesTestService;
import by.pilipuk.coinpulse.repository.PriceRepository;
import by.pilipuk.dto.PriceDto;
import by.pilipuk.dto.PricePageResponseDto;
import by.pilipuk.dto.TrendResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static by.pilipuk.coinpulse.environment.TestData.createAveragePriceDto;
import static by.pilipuk.coinpulse.environment.TestData.createBnbUsdtPrice;
import static by.pilipuk.coinpulse.environment.TestData.createPriceDto;
import static by.pilipuk.coinpulse.environment.TestData.createPricePageResponseDto;
import static by.pilipuk.coinpulse.environment.TestData.createTrendResponseDto;
import static by.pilipuk.coinpulse.environment.TestData.toOffsetDateTime;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration tests for PriceController")
class PriceEntityControllerIntegrationTest extends BaseControllerTest {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private TruncateDbTablesTestService truncateTestService;

    private static final LocalDateTime FIRST_TIME =
            LocalDateTime.of(2026, 9, 1, 10, 30);

    private static final LocalDateTime MIDDLE_TIME =
            LocalDateTime.of(2026, 9, 15, 10, 30);

    private static final LocalDateTime LAST_TIME =
            LocalDateTime.of(2026, 9, 30, 10, 30);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSqlTestContainer.configure(registry);
    }

    @BeforeEach
    void setup() {
        truncateTestService.truncateAllTables();
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - returns latest price")
    void getLatestPrice_success() {
        priceRepository.save(createBnbUsdtPrice(TestData.FIRST_TEST_PRICE, FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice(TestData.THIRD_TEST_PRICE, MIDDLE_TIME));

        PriceDto expectedResponse = createPriceDto(
                TestData.SYMBOL,
                TestData.THIRD_TEST_PRICE,
                toOffsetDateTime(MIDDLE_TIME)
        );

        performGetRequest(
                get("/api/v1/prices/latest").param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                expectedResponse
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - returns 404 when no prices exist")
    void getLatestPrice_notFound() {
        performGetRequest(
                get("/api/v1/prices/latest").param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                null,
                NOT_FOUND.value()
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - returns average price")
    void getAveragePrice_success() {
        priceRepository.save(createBnbUsdtPrice(TestData.FIRST_TEST_PRICE, FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice(TestData.SECOND_TEST_PRICE, LAST_TIME));

        performGetRequest(
                get("/api/v1/prices/average")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                createAveragePriceDto("55000.00000000")
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - returns 404 when no prices in period")
    void getAveragePrice_notFoundInPeriod() {
        performGetRequest(
                get("/api/v1/prices/average")
                        .param(TestData.FROM_LITERAL, "2026-01-01T00:00:00Z")
                        .param(TestData.TO_LITERAL, "2026-01-31T23:59:59Z")
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                null,
                NOT_FOUND.value()
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices - returns paginated prices")
    void getPricesPage_success() {
        int page = 0;
        int size = 2;
        int totalElements = 3;
        boolean hasNext = true;

        priceRepository.save(createBnbUsdtPrice(TestData.FIRST_TEST_PRICE, FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice(TestData.SECOND_TEST_PRICE, MIDDLE_TIME));
        priceRepository.save(createBnbUsdtPrice(TestData.THIRD_TEST_PRICE, LAST_TIME));

        PricePageResponseDto expectedResponse = createPricePageResponseDto(
                List.of(
                        createPriceDto(TestData.SYMBOL, TestData.FIRST_TEST_PRICE, toOffsetDateTime(FIRST_TIME)),
                        createPriceDto(TestData.SYMBOL, TestData.SECOND_TEST_PRICE, toOffsetDateTime(MIDDLE_TIME))
                ),
                page,
                size,
                totalElements,
                hasNext
        );

        performGetRequest(
                get("/api/v1/prices")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL)
                        .param(TestData.PAGE_LITERAL, String.valueOf(page))
                        .param(TestData.SIZE_LITERAL, String.valueOf(size)),
                expectedResponse
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns UPWARD trend")
    void getPriceTrend_upward() {
        testPriceTrend(
                TestData.FIRST_TEST_PRICE,
                TestData.SECOND_TEST_PRICE,
                TrendResponseDto.TrendEnum.UPWARD
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns DOWNWARD trend")
    void getPriceTrend_downward() {
        testPriceTrend(
                TestData.SECOND_TEST_PRICE,
                TestData.FIRST_TEST_PRICE,
                TrendResponseDto.TrendEnum.DOWNWARD
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns FLAT trend")
    void getPriceTrend_flat() {
        testPriceTrend(
                TestData.FIRST_TEST_PRICE,
                TestData.FIRST_TEST_PRICE,
                TrendResponseDto.TrendEnum.FLAT
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - 404 when no prices in period")
    void getPriceTrend_notFoundInPeriod() {
        performGetRequest(
                get("/api/v1/prices/trend")
                        .param(TestData.FROM_LITERAL, "2026-01-01T00:00:00Z")
                        .param(TestData.TO_LITERAL, "2026-01-31T23:59:59Z")
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                null,
                NOT_FOUND.value()
        );
    }

    private void testPriceTrend(
            String firstPrice, String lastPrice, TrendResponseDto.TrendEnum expectedTrend
    ) {
        priceRepository.save(createBnbUsdtPrice(firstPrice, FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice(lastPrice, LAST_TIME));

        TrendResponseDto expectedResponse = createTrendResponseDto(
                TestData.SYMBOL,
                OffsetDateTime.parse(TestData.FIRST_TEST_DATETIME),
                OffsetDateTime.parse(TestData.SECOND_TEST_DATETIME),
                expectedTrend
        );

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                expectedResponse
        );
    }
}
