package by.pilipuk.coinpulse.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionDto {

    private String code;
    private List<ExceptionContext> contexts;
    private String rootCause;

    @Getter
    @Setter
    @FieldNameConstants
    @NoArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder(value = {"code", "field", "message", "object", "parameters"})
    public static class ExceptionContext {

        private String code;
        private String message;
        private String field;
        private String object;
        private Map<String, Object> parameters;

        private ExceptionContext(String code) {
            this.code = code;
        }

        public static ExceptionContext create(String code) {
            return new ExceptionContext(code);
        }

        public ExceptionContext addParameters(
                String key,
                Object value
        ) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }

            parameters.put(key, value);
            return this;
        }
    }
}
