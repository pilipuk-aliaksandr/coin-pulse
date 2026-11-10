package by.pilipuk.coinpulse.exception;

import by.pilipuk.coinpulse.exception.base.BaseApplicationException;
import by.pilipuk.coinpulse.model.dto.ExceptionDto;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.Map;

public final class ApplicationException extends BaseApplicationException {

    @Serial
    private static final long serialVersionUID = 5736399598744972686L;
    private static final String CODE = "COINPULSE_APPLICATION_EXCEPTION";

    private ApplicationException(
            HttpStatus httpStatus, Level logLevel, ExceptionDto.ExceptionContext exceptionContext
    ) {
        super(CODE, httpStatus, logLevel, exceptionContext, null);
    }

    public static ApplicationException create(ApplicationExceptionCode code, Object param) {
        return new ApplicationException(
                code.getHttpStatus(),
                code.getLevel(),
                ExceptionDto.ExceptionContext
                        .create(code.name())
                        .addParameters(code.getKey(), param)
        );
    }

    public static ApplicationException create(ApplicationExceptionCode code, Map<String, ?> params) {
        ExceptionDto.ExceptionContext context = ExceptionDto.ExceptionContext.create(code.name());
        params.forEach(context::addParameters);
        return new ApplicationException(code.getHttpStatus(), code.getLevel(), context);
    }
}
