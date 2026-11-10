package by.pilipuk.coinpulse.client;

import by.pilipuk.coinpulse.client.exception.BinanceIntegrationException;
import by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode;
import by.pilipuk.coinpulse.environment.PostgreSqlTestContainer;
import by.pilipuk.coinpulse.environment.TestData;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@EnableWireMock
@ActiveProfiles("test")
@DisplayName("Integration tests for BinanceClient")
class BinanceClientIntegrationTest {

    private static final String AVG_PRICE_PATH = "/api/v3/avgPrice";

    @Autowired
    private BinanceClient binanceClient;

    @InjectWireMock
    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSqlTestContainer.configure(registry);
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Should return average price")
    void fetchAveragePrice_success() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(TestData.CONTENT_TYPE_LITERAL, TestData.CONTENT_TYPE)
                        .withBody(
                                """
                                        {
                                            "mins": 5,
                                            "price": "50000.00000000",
                                            "closeTime": 1694061154503
                                        }
                                """)));

        BigDecimal actualPrice = binanceClient.fetchAveragePrice(TestData.SYMBOL);

        assertThat(actualPrice)
                .isEqualByComparingTo(TestData.FIRST_TEST_PRICE);

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL)));
    }

    @Test
    @DisplayName("Should throw exception when symbol is invalid")
    void fetchAveragePrice_invalidSymbol() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(TestData.CONTENT_TYPE_LITERAL, TestData.CONTENT_TYPE)
                        .withBody(
                                """
                                        {
                                            "code": -1121,
                                            "msg": "Invalid symbol."
                                        }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(TestData.SYMBOL))
                .isInstanceOfSatisfying(
                        BinanceIntegrationException.class,
                        exception -> assertAll(
                            () -> assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_INVALID_SYMBOL))
                                    .isTrue(),

                            () -> assertThat(exception.getParameter(TestData.SYMBOL_LITERAL))
                                    .isEqualTo(TestData.SYMBOL),

                            () -> assertThat(exception.getParameter("httpStatus"))
                                    .isEqualTo(400),

                            () -> assertThat(exception.getParameter("responseBody"))
                                    .asString()
                                    .contains("Invalid symbol")
                        ));
    }

    @Test
    @DisplayName("Should throw rate limit exception and resolve Retry-After")
    void fetchAveragePrice_rateLimit() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "60")
                        .withHeader(TestData.CONTENT_TYPE_LITERAL, TestData.CONTENT_TYPE)
                        .withBody(
                                """
                                        {
                                            "code": -1003,
                                            "msg": "Too many requests."
                                        }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(TestData.SYMBOL))
                .isInstanceOfSatisfying(
                        BinanceIntegrationException.class,
                        exception -> assertAll(
                            () -> assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_RATE_LIMIT))
                                    .isTrue(),

                            () -> assertThat(exception.getParameter("retryAfterSeconds"))
                                    .isEqualTo(60L),

                            () -> assertThat(exception.getParameter("httpStatus"))
                                    .isEqualTo(429)
                        ));
    }

    @Test
    @DisplayName("Should throw exception when price is zero")
    void fetchAveragePrice_invalidPrice() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(TestData.CONTENT_TYPE_LITERAL, TestData.CONTENT_TYPE)
                        .withBody(
                                """
                                        {
                                            "mins": 5,
                                            "price": "0",
                                            "closeTime": 1694061154503
                                        }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(TestData.SYMBOL))
                .isInstanceOfSatisfying(
                        BinanceIntegrationException.class,
                        exception -> assertAll(
                            () -> assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_INVALID_PRICE))
                                    .isTrue(),

                            () -> assertThat(exception.getParameter(TestData.SYMBOL_LITERAL))
                                    .isEqualTo(TestData.SYMBOL)
                        ));
    }

    @Test
    @DisplayName("Should throw connection exception when response JSON is invalid")
    void fetchAveragePrice_invalidJson() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(TestData.SYMBOL_LITERAL, equalTo(TestData.SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(TestData.CONTENT_TYPE_LITERAL, TestData.CONTENT_TYPE)
                        .withBody("invalid json")));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(TestData.SYMBOL))
                .isInstanceOfSatisfying(
                        BinanceIntegrationException.class,
                        exception -> assertAll(
                            () -> assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_RESPONSE_ERROR))
                                    .isTrue(),

                            () -> assertThat(exception.getParameter(TestData.SYMBOL_LITERAL))
                                    .isEqualTo(TestData.SYMBOL)
                        ));
    }
}
