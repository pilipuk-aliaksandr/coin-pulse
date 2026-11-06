
package by.pilipuk.coinpulse.controller.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected void performGetRequest(
            MockHttpServletRequestBuilder request, Object expectedResponse
    ) throws Exception {
        performGetRequest(request, expectedResponse, 200);
    }

    protected void performGetRequest(
            MockHttpServletRequestBuilder request, Object expectedResponse, int expectedStatus
    ) throws Exception {
        MvcResult result = mockMvc.perform(request
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .isEqualTo(expectedStatus);

        if (expectedResponse != null) {
            assertThat(result.getResponse().getContentType())
                    .contains(MediaType.APPLICATION_JSON_VALUE);

            Object actualResponse = objectMapper
                    .readerFor(expectedResponse.getClass())
                    .readValue(result.getResponse().getContentAsString());

            assertThat(actualResponse)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(expectedResponse);
        }
    }
}
