package by.pilipuk.coinpulse.client;

import by.pilipuk.coinpulse.client.exception.BinanceIntegrationException;
import by.pilipuk.coinpulse.client.exception.BinanceIntegrationExceptionCode;
import by.pilipuk.coinpulse.environment.PostgreSqlTestContainer;
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

@SpringBootTest
@EnableWireMock
@ActiveProfiles("test")
@DisplayName("Integration tests for BinanceClient")
public class BinanceClientIntegrationTest {

    private static final String AVG_PRICE_PATH = "/api/v3/avgPrice";
    private static final String SYMBOL = "BNBUSDT";

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
                .withQueryParam("symbol", equalTo(SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                                {
                                                    "mins": 5,
                                                    "price": "67432.12345678",
                                                    "closeTime": 1694061154503
                                                }
                                """)));

        BigDecimal actualPrice = binanceClient.fetchAveragePrice(SYMBOL);

        assertThat(actualPrice)
                .isEqualByComparingTo("67432.12345678");

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam("symbol", equalTo(SYMBOL)));
    }

    @Test
    @DisplayName("Should throw exception when symbol is invalid")
    void fetchAveragePrice_invalidSymbol() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam("symbol", equalTo(SYMBOL))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                                {
                                                    "code": -1121,
                                                    "msg": "Invalid symbol."
                                                }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(SYMBOL))
                .isInstanceOfSatisfying(BinanceIntegrationException.class, exception -> {
                    assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_INVALID_SYMBOL))
                            .isTrue();

                    assertThat(exception.getParameter("symbol"))
                            .isEqualTo(SYMBOL);

                    assertThat(exception.getParameter("httpStatus"))
                            .isEqualTo(400);

                    assertThat(exception.getParameter("responseBody"))
                            .asString()
                            .contains("Invalid symbol");
                });
    }

    @Test
    @DisplayName("Should throw rate limit exception and resolve Retry-After")
    void fetchAveragePrice_rateLimit() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam("symbol", equalTo(SYMBOL))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "60")
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                        {
                                            "code": -1003,
                                            "msg": "Too many requests."
                                        }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(SYMBOL))
                .isInstanceOfSatisfying(BinanceIntegrationException.class, exception -> {
                    assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_RATE_LIMIT))
                            .isTrue();

                    assertThat(exception.getParameter("retryAfterSeconds"))
                            .isEqualTo(60L);

                    assertThat(exception.getParameter("httpStatus"))
                            .isEqualTo(429);
                });
    }

    @Test
    @DisplayName("Should throw exception when price is zero")
    void fetchAveragePrice_invalidPrice() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam("symbol", equalTo(SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                        {
                                            "mins": 5,
                                            "price": "0",
                                            "closeTime": 1694061154503
                                        }
                                """)));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(SYMBOL))
                .isInstanceOfSatisfying(BinanceIntegrationException.class, exception -> {
                    assertThat(exception.hasCode(BinanceIntegrationExceptionCode.BINANCE_INVALID_PRICE))
                            .isTrue();

                    assertThat(exception.getParameter("symbol"))
                            .isEqualTo(SYMBOL);
                });
    }

    @Test
    @DisplayName("Should throw connection exception when response JSON is invalid")
    void fetchAveragePrice_invalidJson() {
        wireMockServer.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam("symbol", equalTo(SYMBOL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("invalid json")));

        assertThatThrownBy(() -> binanceClient.fetchAveragePrice(SYMBOL))
                .isInstanceOfSatisfying(BinanceIntegrationException.class, exception -> {
                    assertThat(exception.hasCode(
                            BinanceIntegrationExceptionCode.BINANCE_RESPONSE_ERROR
                    )).isTrue();

                    assertThat(exception.getParameter("symbol"))
                            .isEqualTo(SYMBOL);
                });
    }
}
