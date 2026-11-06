package by.pilipuk.coinpulse.mapper;

import by.pilipuk.coinpulse.exception.base.BaseApplicationException;
import by.pilipuk.coinpulse.model.dto.ExceptionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = {Instant.class}
)
public abstract class ApplicationExceptionMapper {

    @Mapping(target = "contexts", expression = "java(java.util.List.of(ex.getExceptionContext()))")
    @Mapping(target = "rootCause", expression = "java(ex.getCause() != null ? ex.getCause().getMessage() : null)")
    public abstract ExceptionDto toExceptionDto(BaseApplicationException ex);

    public ExceptionDto toValidationDto(String message) {
        var context = ExceptionDto.ExceptionContext.create("INVALID_PARAMETER").setMessage(message);
        return new ExceptionDto("REQUEST_VALIDATION", List.of(context), null);
    }

    public ExceptionDto toUnexpectedDto() {
        var context = ExceptionDto.ExceptionContext.create("INTERNAL_SERVER_ERROR");
        return new ExceptionDto("UNEXPECTED_EXCEPTION", List.of(context), null);
    }
}
