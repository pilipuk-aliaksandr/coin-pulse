package by.pilipuk.coinpulse.client.exception;

import lombok.Getter;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@Getter
public enum BinanceIntegrationExceptionCode {
    BINANCE_REQUEST_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, Level.ERROR),
    BINANCE_INVALID_SYMBOL(HttpStatus.BAD_REQUEST, Level.ERROR),
    BINANCE_INVALID_PRICE(HttpStatus.BAD_GATEWAY, Level.ERROR),
    BINANCE_RATE_LIMIT(HttpStatus.SERVICE_UNAVAILABLE, Level.ERROR),
    BINANCE_HTTP_ERROR(HttpStatus.BAD_GATEWAY, Level.ERROR),
    BINANCE_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, Level.ERROR);

    private final HttpStatus httpStatus;
    private final Level level;
    private final String key;

    BinanceIntegrationExceptionCode(HttpStatus httpStatus, Level level, String key) {
        this.httpStatus = httpStatus;
        this.level = level;
        this.key = key;
    }

    BinanceIntegrationExceptionCode(HttpStatus httpStatus, Level level) {
        this(httpStatus, level, null);
    }
}
