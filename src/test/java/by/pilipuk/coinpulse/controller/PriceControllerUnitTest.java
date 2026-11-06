
package by.pilipuk.coinpulse.controller;

import by.pilipuk.coinpulse.controller.base.BaseControllerTest;
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

import static by.pilipuk.coinpulse.environment.PriceTestFactory.createAveragePriceDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createPriceDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createPricePageResponseDto;
import static by.pilipuk.coinpulse.environment.PriceTestFactory.createTrendResponseDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@DisplayName("Unit tests for PriceController")
class PriceControllerUnitTest extends BaseControllerTest {

    @MockitoBean
    private PriceService priceService;

    @MockitoBean
    private ApplicationExceptionMapper exceptionMapper;

    @MockitoBean(name = "jpaMappingContext", enforceOverride = true)
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("GET /api/v1/prices/latest - success")
    void getLatestPrice_success() throws Exception {
        var response = createPriceDto(
                "BNBUSDT",
                "61234.56780000",
                OffsetDateTime.parse("2026-09-15T10:30:00Z")
        );

        when(priceService.getLatestPrice("BNBUSDT")).thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/latest").param("symbol", "BNBUSDT"),
                response
        );

        verify(priceService).getLatestPrice("BNBUSDT");
    }

    @Test
    @DisplayName("GET /api/v1/prices/latest - price not found")
    void getLatestPrice_notFound() throws Exception {
        when(priceService.getLatestPrice("NONEXIST"))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.PRICE_NOT_FOUND,
                        "NONEXIST"
                ));

        performGetRequest(
                get("/api/v1/prices/latest").param("symbol", "NONEXIST"),
                null,
                NOT_FOUND.value()
        );

        verify(priceService).getLatestPrice("NONEXIST");
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - success")
    void getAveragePrice_success() throws Exception {
        var response = createAveragePriceDto("58432.12345678");

        when(priceService.getAveragePrice(any(), any(), eq("BNBUSDT")))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/average")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                response
        );

        verify(priceService).getAveragePrice(any(), any(), eq("BNBUSDT"));
    }

    @Test
    @DisplayName("GET /api/v1/prices/average - invalid period")
    void getAveragePrice_invalidPeriod() throws Exception {
        when(priceService.getAveragePrice(any(), any(), eq("BNBUSDT")))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.INVALID_PERIOD,
                        Map.of("from", "2026-10-01", "to", "2026-09-01")
                ));

        performGetRequest(
                get("/api/v1/prices/average")
                        .param("from", "2026-10-01T00:00:00Z")
                        .param("to", "2026-09-01T00:00:00Z")
                        .param("symbol", "BNBUSDT"),
                null,
                BAD_REQUEST.value()
        );

        verify(priceService).getAveragePrice(any(), any(), eq("BNBUSDT"));
    }

    @Test
    @DisplayName("GET /api/v1/prices - success")
    void getPricesPage_success() throws Exception {
        int page = 0;
        int size = 10;
        int totalElements = 1;
        boolean hasNext = false;

        var item = createPriceDto(
                "BNBUSDT",
                "50000.00000000",
                OffsetDateTime.parse("2026-09-15T10:30:00Z")
        );

        var response = createPricePageResponseDto(List.of(item), page, size, totalElements, hasNext);

        when(priceService.getPrices(any(), any(), eq("BNBUSDT"), eq(0), eq(10)))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT")
                        .param("page", "0")
                        .param("size", "10"),
                response
        );

        verify(priceService).getPrices(any(), any(), eq("BNBUSDT"), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend — success")
    void getPriceTrend_success() throws Exception {
        var response = createTrendResponseDto(
                "BNBUSDT",
                OffsetDateTime.parse("2026-09-01T00:00:00Z"),
                OffsetDateTime.parse("2026-09-30T23:59:59Z"),
                TrendResponseDto.TrendEnum.UPWARD
        );

        when(priceService.getTrend(any(), any(), eq("BNBUSDT")))
                .thenReturn(response);

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                response
        );

        verify(priceService).getTrend(any(), any(), eq("BNBUSDT"));
    }

    @Test
    @DisplayName("GET /api/v1/prices/trend — no prices in period")
    void getPriceTrend_notFoundInPeriod() throws Exception {
        when(priceService.getTrend(any(), any(), eq("BNBUSDT")))
                .thenThrow(ApplicationException.create(
                        ApplicationExceptionCode.PRICE_NOT_FOUND_IN_PERIOD,
                        Map.of("symbol", "BNBUSDT")
                ));

        performGetRequest(
                get("/api/v1/prices/trend")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-31T23:59:59Z")
                        .param("symbol", "BNBUSDT"),
                null,
                NOT_FOUND.value()
        );

        verify(priceService).getTrend(any(), any(), eq("BNBUSDT"));
    }
}
