
package by.pilipuk.coinpulse.controller;

import by.pilipuk.coinpulse.controller.base.BaseControllerTest;
import by.pilipuk.coinpulse.environment.TestData;
import by.pilipuk.coinpulse.exception.ApplicationException;
import by.pilipuk.coinpulse.exception.ApplicationExceptionCode;
import by.pilipuk.coinpulse.mapper.ApplicationExceptionMapper;
import by.pilipuk.coinpulse.service.PriceService;
import by.pilipuk.dto.TrendResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@DisplayName("Unit tests for PriceController")
class PriceEntityControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private PriceService priceService;

    @MockitoBean
    private ApplicationExceptionMapper exceptionMapper;

    @MockitoBean(name = "jpaMappingContext", enforceOverride = true)
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("GET /api/v1/prices/latest - success")
    void getLatestPrice_success() {
        var response = TestData.createPriceDto(
                TestData.SYMBOL,
                TestData.FIRST_TEST_PRICE,
                OffsetDateTime.parse(TestData.FIRST_TEST_DATETIME)
        );

        when(priceService.getLatestPrice(TestData.SYMBOL)).thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/latest").param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                response
        );

        verify(priceService).getLatestPrice(TestData.SYMBOL);
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - price not found")
    void getLatestPrice_notFound() {
        when(priceService.getLatestPrice(TestData.NONEXIST_LITERAL))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.PRICE_NOT_FOUND,
                        TestData.NONEXIST_LITERAL
                ));

        performGetRequest(
                get("/api/v1/prices/latest").param(TestData.SYMBOL_LITERAL, TestData.NONEXIST_LITERAL),
                null,
                NOT_FOUND.value()
        );

        verify(priceService).getLatestPrice(TestData.NONEXIST_LITERAL);
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - success")
    void getAveragePrice_success() {
        var response = TestData.createAveragePriceDto(TestData.FIRST_TEST_PRICE);

        when(priceService.getAveragePrice(any(), any(), eq(TestData.SYMBOL)))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/average")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                response
        );

        verify(priceService).getAveragePrice(any(), any(), eq(TestData.SYMBOL));
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - invalid period")
    void getAveragePrice_invalidPeriod() {
        when(priceService.getAveragePrice(any(), any(), eq(TestData.SYMBOL)))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.INVALID_PERIOD,
                        Map.of(TestData.FROM_LITERAL, "2026-10-01", TestData.TO_LITERAL, "2026-09-01")
                ));

        performGetRequest(
                get("/api/v1/prices/average")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                null,
                BAD_REQUEST.value()
        );

        verify(priceService).getAveragePrice(any(), any(), eq(TestData.SYMBOL));
    }

    @Test
    @DisplayName("GET /api/v1/prices - success")
    void getPricesPage_success() {
        int page = 0;
        int size = 10;
        int totalElements = 1;
        boolean hasNext = false;

        var item = TestData.createPriceDto(
                TestData.SYMBOL,
                TestData.FIRST_TEST_PRICE,
                OffsetDateTime.parse(TestData.FIRST_TEST_DATETIME)
        );

        var response = TestData.createPricePageResponseDto(List.of(item), page, size, totalElements, hasNext);

        when(priceService.getPrices(any(), any(), eq(TestData.SYMBOL), eq(0), eq(10)))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL)
                        .param(TestData.PAGE_LITERAL, String.valueOf(page))
                        .param(TestData.SIZE_LITERAL, String.valueOf(size)),
                response
        );

        verify(priceService).getPrices(any(), any(), eq(TestData.SYMBOL), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend — success")
    void getPriceTrend_success() {
        var response = TestData.createTrendResponseDto(
                TestData.SYMBOL,
                OffsetDateTime.parse(TestData.FIRST_TEST_DATETIME),
                OffsetDateTime.parse(TestData.SECOND_TEST_DATETIME),
                TrendResponseDto.TrendEnum.UPWARD
        );

        when(priceService.getTrend(any(), any(), eq(TestData.SYMBOL)))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                response
        );

        verify(priceService).getTrend(any(), any(), eq(TestData.SYMBOL));
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend — no prices in period")
    void getPriceTrend_notFoundInPeriod() {
        when(priceService.getTrend(any(), any(), eq(TestData.SYMBOL)))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.PRICE_NOT_FOUND_IN_PERIOD,
                        Map.of(TestData.SYMBOL_LITERAL, TestData.SYMBOL)
                ));

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param(TestData.FROM_LITERAL, TestData.FIRST_TEST_DATETIME)
                        .param(TestData.TO_LITERAL, TestData.SECOND_TEST_DATETIME)
                        .param(TestData.SYMBOL_LITERAL, TestData.SYMBOL),
                null,
                NOT_FOUND.value()
        );

        verify(priceService).getTrend(any(), any(), eq(TestData.SYMBOL));
    }
}
