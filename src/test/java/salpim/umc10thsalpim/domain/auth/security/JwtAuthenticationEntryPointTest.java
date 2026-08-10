package salpim.umc10thsalpim.domain.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtAuthenticationEntryPoint entryPoint =
            new JwtAuthenticationEntryPoint(objectMapper);

    @Test
    void writesStandardApiResponseForInvalidAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setAttribute(
                JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE,
                AuthErrorCode.INVALID_TOKEN
        );

        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("authentication required")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(body.get("isSuccess").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo("AUTH401_TOKEN");
        assertThat(body.get("message").asText()).isEqualTo("유효하지 않은 토큰입니다.");
        assertThat(body.get("result").isNull()).isTrue();
    }
}
