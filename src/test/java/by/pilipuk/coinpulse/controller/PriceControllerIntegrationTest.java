
package by.pilipuk.coinpulse.controller;

import by.pilipuk.coinpulse.controller.base.BaseControllerTest;
import by.pilipuk.coinpulse.environment.PostgreSqlTestContainer;
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

import static by.pilipuk.coinpulse.environment.PriceTestFactory.createAveragePriceDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createBnbUsdtPrice;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createPriceDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createPricePageResponseDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createTrendResponseDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.toOffsetDateTime;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration tests for PriceController")
class PriceControllerIntegrationTest extends BaseControllerTest {

    @Autowired
    private PriceRepository priceRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSqlTestContainer.configure(registry);
    }

    @Autowired
    private TruncateDbTablesTestService truncateTestService;

    private static final LocalDateTime FIRST_TIME =
            LocalDateTime.of(2026, 9, 1, 10, 30);

    private static final LocalDateTime MIDDLE_TIME =
            LocalDateTime.of(2026, 9, 15, 10, 30);

    private static final LocalDateTime LAST_TIME =
            LocalDateTime.of(2026, 9, 30, 10, 30);

    @BeforeEach
    void setup() {
        truncateTestService.truncateAllTables();
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - returns latest price")
    void getLatestPrice_success() throws Exception {
        priceRepository.save(createBnbUsdtPrice("50000.00000000", FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice("61234.56780000", MIDDLE_TIME));

        PriceDto expectedResponse = createPriceDto(
                "BNBUSDT",
                "61234.56780000",
                toOffsetDateTime(MIDDLE_TIME)
        );

        performGetRequest(
                get("/api/v1/prices/latest").param("symbol", "BNBUSDT"),
                expectedResponse
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - returns 404 when no prices exist")
    void getLatestPrice_notFound() throws Exception {
        performGetRequest(
                get("/api/v1/prices/latest").param("symbol", "BNBUSDT"),
                null,
                NOT_FOUND.value()
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - returns average price")
    void getAveragePrice_success() throws Exception {
        priceRepository.save(createBnbUsdtPrice("50000.00000000", FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice("60000.00000000", LAST_TIME));

        performGetRequest(
                get("/api/v1/prices/average")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                createAveragePriceDto("55000.00000000")
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - returns 404 when no prices in period")
    void getAveragePrice_notFoundInPeriod() throws Exception {
        performGetRequest(
                get("/api/v1/prices/average")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-31T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                null,
                NOT_FOUND.value()
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices - returns paginated prices")
    void getPricesPage_success() throws Exception {
        int page = 0;
        int size = 2;
        int totalElements = 3;
        boolean hasNext = true;

        priceRepository.save(createBnbUsdtPrice("50000.00000000", FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice("60000.00000000", MIDDLE_TIME));
        priceRepository.save(createBnbUsdtPrice("70000.00000000", LAST_TIME));

        PricePageResponseDto expectedResponse = createPricePageResponseDto(
                List.of(
                        createPriceDto("BNBUSDT", "50000.00000000", toOffsetDateTime(FIRST_TIME)),
                        createPriceDto("BNBUSDT", "60000.00000000", toOffsetDateTime(MIDDLE_TIME))
                ),
                page,
                size,
                totalElements,
                hasNext
        );

        performGetRequest(
                get("/api/v1/prices")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT")
                        .param("page", "0")
                        .param("size", "2"),
                expectedResponse
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns UPWARD trend")
    void getPriceTrend_upward() throws Exception {
        testPriceTrend(
                "50000.00000000",
                "60000.00000000",
                TrendResponseDto.TrendEnum.UPWARD
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns DOWNWARD trend")
    void getPriceTrend_downward() throws Exception {
        testPriceTrend(
                "60000.00000000",
                "50000.00000000",
                TrendResponseDto.TrendEnum.DOWNWARD
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - returns FLAT trend")
    void getPriceTrend_flat() throws Exception {
        testPriceTrend(
                "55000.00000000",
                "55000.00000000",
                TrendResponseDto.TrendEnum.FLAT
        );
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend - 404 when no prices in period")
    void getPriceTrend_notFoundInPeriod() throws Exception {
        performGetRequest(
                get("/api/v1/prices/trend")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-31T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                null,
                NOT_FOUND.value()
        );
    }

    private void testPriceTrend(
            String firstPrice, String lastPrice, TrendResponseDto.TrendEnum expectedTrend
    ) throws Exception {
        priceRepository.save(createBnbUsdtPrice(firstPrice, FIRST_TIME));
        priceRepository.save(createBnbUsdtPrice(lastPrice, LAST_TIME));

        TrendResponseDto expectedResponse = createTrendResponseDto(
                "BNBUSDT",
                OffsetDateTime.parse("2026-09-01T00:00:00Z"),
                OffsetDateTime.parse("2026-09-30T23:59:59Z"),
                expectedTrend
        );

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                expectedResponse
        );
    }
}
