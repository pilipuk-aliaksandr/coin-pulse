package by.pilipuk.coinpulse.controller;

import by.pilipuk.coinpulse.exception.base.BaseApplicationException;
import by.pilipuk.coinpulse.mapper.ApplicationExceptionMapper;
import by.pilipuk.coinpulse.model.dto.ExceptionDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ApplicationExceptionMapper exceptionMapper;

    @ExceptionHandler(BaseApplicationException.class)
    public ExceptionDto handleBaseApplicationException(
            BaseApplicationException ex, HttpServletResponse response
    ) {
        switch (ex.getLogLevel()) {
            case ERROR -> log.error("[ERROR] {}", ex.getMessage(), ex);
            case DEBUG -> log.debug("[DEBUG] {}", ex.getMessage());
            default -> log.info("[INFO] {}", ex.getMessage());
        }

        response.setStatus(ex.getHttpStatus().value());
        return exceptionMapper.toExceptionDto(ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ExceptionDto handleConstraintViolation(ConstraintViolationException ex) {
        log.debug("Request validation failed: {}", ex.getMessage());
        return exceptionMapper.toValidationDto(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ExceptionDto handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.debug("Invalid parameter {}: {}", ex.getName(), ex.getValue());
        return exceptionMapper.toValidationDto("Invalid value for parameter '%s'".formatted(ex.getName()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionDto handleOtherException(Exception ex) {
        log.error("[ERROR] {}", ex.getMessage(), ex);
        return exceptionMapper.toUnexpectedDto();
    }
}
