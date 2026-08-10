package salpim.umc10thsalpim.domain.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String AUTH_ERROR_ATTRIBUTE =
            JwtAuthenticationEntryPoint.class.getName() + ".AUTH_ERROR";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException, ServletException {
        BaseErrorCode errorCode = resolveErrorCode(request);

        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.onFailure(errorCode, null)
        );
    }

    private BaseErrorCode resolveErrorCode(HttpServletRequest request) {
        Object errorCode = request.getAttribute(AUTH_ERROR_ATTRIBUTE);
        if (errorCode instanceof BaseErrorCode baseErrorCode) {
            return baseErrorCode;
        }
        return AuthErrorCode.INVALID_TOKEN;
    }
}
