package by.pilipuk.coinpulse.client;

import by.pilipuk.coinpulse.client.dto.BinanceAvgPriceResponse;
import by.pilipuk.coinpulse.client.dto.BinanceErrorResponse;
import by.pilipuk.coinpulse.client.exception.BinanceIntegrationException;
import by.pilipuk.coinpulse.client.property.BinanceProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_HTTP_ERROR;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_INVALID_PRICE;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_INVALID_SYMBOL;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_RATE_LIMIT;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_REQUEST_CONNECTION_FAILED;
import static by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode.BINANCE_RESPONSE_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceClient {

    private static final String AVG_PRICE_URI = "/api/v3/avgPrice?symbol={symbol}";
    private static final String SYMBOL_LITERAL = "symbol";
    private static final int INVALID_SYMBOL_CODE = -1121;

    private final RestClient binanceRestClient;
    private final BinanceProperties binanceProperties;
    private final ObjectMapper objectMapper;

    public BigDecimal fetchAveragePrice(String symbol) {
        try {
            BinanceAvgPriceResponse response = binanceRestClient.get()
                    .uri(AVG_PRICE_URI, symbol)
                    .retrieve()
                    .body(BinanceAvgPriceResponse.class);

            return validateAndGetPrice(response, symbol);
        } catch (RestClientResponseException exception) {
            throw mapToCustomException(exception, symbol);
        } catch (ResourceAccessException exception) {
            throw BinanceIntegrationException.create(
                    BINANCE_REQUEST_CONNECTION_FAILED,
                    Map.of(
                            SYMBOL_LITERAL, symbol,
                            "reason", String.valueOf(exception.getMessage())
                    ),
                    exception
            );
        } catch (RestClientException exception) {
            throw BinanceIntegrationException.create(
                    BINANCE_RESPONSE_ERROR,
                    Map.of(
                            SYMBOL_LITERAL, symbol,
                            "reason", String.valueOf(exception.getMessage())
                    ),
                    exception
            );
        }
    }

    private BigDecimal validateAndGetPrice(BinanceAvgPriceResponse response, String symbol) {
        if (response == null || response.price() == null || response.price().signum() <= 0) {
            throw BinanceIntegrationException.create(
                    BINANCE_INVALID_PRICE,
                    Map.of(SYMBOL_LITERAL, symbol, "response", String.valueOf(response))
            );
        }

        return response.price();
    }

    private BinanceIntegrationException mapToCustomException(
            RestClientResponseException exception, String symbol
    ) {
        int status = exception.getStatusCode().value();

        if (status == HttpStatus.TOO_MANY_REQUESTS.value()
                || status == HttpStatus.I_AM_A_TEAPOT.value()) {
            return BinanceIntegrationException.create(
                    BINANCE_RATE_LIMIT,
                    Map.of(
                            SYMBOL_LITERAL, symbol,
                            "httpStatus", status,
                            BinanceIntegrationException.RETRY_AFTER_SECONDS,
                            resolveRetryAfter(exception.getResponseHeaders()).toSeconds()
                    ),
                    exception
            );
        }

        String responseBody = exception.getResponseBodyAsString();

        if (isInvalidSymbol(responseBody)) {
            return BinanceIntegrationException.create(
                    BINANCE_INVALID_SYMBOL,
                    Map.of(
                            SYMBOL_LITERAL, symbol,
                            "httpStatus", status,
                            "responseBody", responseBody
                    ),
                    exception
            );
        }

        return BinanceIntegrationException.create(
                BINANCE_HTTP_ERROR,
                Map.of(
                        SYMBOL_LITERAL, symbol,
                        "httpStatus", status,
                        "responseBody", responseBody
                ),
                exception
        );
    }

    private boolean isInvalidSymbol(String responseBody) {
        try {
            BinanceErrorResponse error = objectMapper.readValue(responseBody, BinanceErrorResponse.class);

            return error != null && Integer.valueOf(INVALID_SYMBOL_CODE).equals(error.code());
        } catch (JsonProcessingException exception) {
            return false;
        }
    }

    private Duration resolveRetryAfter(MultiValueMap<String, String> headers) {
        String value = headers == null
                ? null
                : headers.getFirst(HttpHeaders.RETRY_AFTER);

        if (value != null) {
            try {
                long seconds = Long.parseLong(value.strip());

                if (seconds > 0) {
                    return Duration.ofSeconds(seconds);
                }
            } catch (NumberFormatException exception) {
                log.warn("Cannot parse Retry-After header '{}', using default backoff", value);
            }
        }

        return binanceProperties.rateLimitBackoff();
    }
}
