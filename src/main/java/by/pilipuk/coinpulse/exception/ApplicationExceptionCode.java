package by.pilipuk.coinpulse.exception;

import lombok.Getter;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@Getter
public enum ApplicationExceptionCode {
    PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, Level.INFO, "symbol"),
    PRICE_NOT_FOUND_IN_PERIOD(HttpStatus.NOT_FOUND, Level.INFO),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, Level.INFO),
    FAILED_TO_CONVERT_TO_JSON(HttpStatus.INTERNAL_SERVER_ERROR, Level.INFO);

    private final HttpStatus httpStatus;
    private final Level level;
    private final String key;

    ApplicationExceptionCode(HttpStatus httpStatus, Level level, String key) {
        this.httpStatus = httpStatus;
        this.level = level;
        this.key = key;
    }

    ApplicationExceptionCode(HttpStatus httpStatus, Level level) {
        this(httpStatus, level, null);
    }
}
