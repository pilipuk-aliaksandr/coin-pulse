package by.pilipuk.coinpulse.exception.base;

import by.pilipuk.coinpulse.model.dto.ExceptionDto;
import lombok.Getter;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;

import static java.util.Optional.ofNullable;

@Getter
public abstract class BaseApplicationException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;
    private final Level logLevel;
    private final ExceptionDto.ExceptionContext exceptionContext;

    @Serial
    private static final long serialVersionUID = -6492338621056190463L;
    private static final String PARAMETRIZED_MESSAGE = "Code: %s, params: [%s]";
    private static final String MESSAGE = "Code: %s";

    protected BaseApplicationException(
            String code,
            HttpStatus httpStatus,
            Level logLevel,
            ExceptionDto.ExceptionContext exceptionContext,
            Throwable cause
    ) {
        super(createSuperMessage(exceptionContext), cause);
        this.httpStatus = httpStatus;
        this.exceptionContext = exceptionContext;
        this.code = code;
        this.logLevel = logLevel;
    }

    private static String createSuperMessage(ExceptionDto.ExceptionContext exceptionContext) {
        return ofNullable(exceptionContext.getParameters())
                .map(parameters -> PARAMETRIZED_MESSAGE.formatted(exceptionContext.getCode(), parameters))
                .orElse(MESSAGE.formatted(exceptionContext.getCode()));
    }
}
