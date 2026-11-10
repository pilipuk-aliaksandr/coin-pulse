
package by.pilipuk.coinpulse.controller.base;

import by.pilipuk.coinpulse.exception.ApplicationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import static by.pilipuk.coinpulse.exception.ApplicationExceptionCode.FAILED_JSON_CONVERTATION;
import static by.pilipuk.coinpulse.exception.ApplicationExceptionCode.MOCK_MVC_PERFORM_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected void performGetRequest(MockHttpServletRequestBuilder request, Object expectedResponse) {
        performGetRequest(request, expectedResponse, 200);
    }

    protected void performGetRequest(
            MockHttpServletRequestBuilder request, Object expectedResponse, int expectedStatus
    ) {
        MvcResult result;
        try {
            result = mockMvc.perform(request.accept(MediaType.APPLICATION_JSON)).andReturn();
        } catch (Exception e) {
            throw ApplicationException.create(MOCK_MVC_PERFORM_EXCEPTION, Map.of("message", e.getMessage()));
        }

        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);

        if (expectedResponse != null) {
            Object actualResponse;
            try {
                actualResponse = objectMapper
                        .readerFor(expectedResponse.getClass())
                        .readValue(result.getResponse().getContentAsString());
            } catch (JsonProcessingException | UnsupportedEncodingException e) {
                throw ApplicationException.create(FAILED_JSON_CONVERTATION, Map.of("message", e.getMessage()));
            }

            MvcResult finalResult = result;
            Object finalActualResponse = actualResponse;
            assertAll(
                    () -> assertThat(finalResult.getResponse().getContentType())
                            .contains(MediaType.APPLICATION_JSON_VALUE),

                    () -> assertThat(finalActualResponse)
                            .usingRecursiveComparison()
                            .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                            .isEqualTo(expectedResponse)
            );
        }
    }
}
