package by.pilipuk.coinpulse.client.exception;

import by.pilipuk.coinpulse.exception.base.BaseApplicationException;
import by.pilipuk.coinpulse.model.dto.ExceptionDto;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.Map;

public final class BinanceIntegrationException extends BaseApplicationException {

    @Serial
    private static final long serialVersionUID = -7734664628906113996L;
    private static final String CODE = "BINANCE_INTEGRATION_EXCEPTION";
    public static final String RETRY_AFTER_SECONDS = "retryAfterSeconds";

    private BinanceIntegrationException(
            HttpStatus httpStatus, Level logLevel, ExceptionDto.ExceptionContext exceptionContext, Throwable cause
    ) {
        super(CODE, httpStatus, logLevel, exceptionContext, cause);
    }

    public static BinanceIntegrationException create(
            BinanceIntegrationExceptionCode code, Map<String, ?> params
    ) {
        ExceptionDto.ExceptionContext context = ExceptionDto.ExceptionContext.create(code.name());
        params.forEach(context::addParameters);
        return new BinanceIntegrationException(code.getHttpStatus(), code.getLevel(), context, null);
    }

    public static BinanceIntegrationException create(
            BinanceIntegrationExceptionCode code, Map<String, ?> params, Throwable cause
    ) {
        ExceptionDto.ExceptionContext context = ExceptionDto.ExceptionContext.create(code.name());
        params.forEach(context::addParameters);
        return new BinanceIntegrationException(code.getHttpStatus(), code.getLevel(), context, cause);
    }

    public boolean hasCode(BinanceIntegrationExceptionCode code) {
        return code.name().equals(getExceptionContext().getCode());
    }

    public Object getParameter(String key) {
        Map<String, Object> params = getExceptionContext().getParameters();
        return params == null ? null : params.get(key);
    }
}
