package salpim.umc10thsalpim.domain.auth.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH_BYTES = 32;

    @NotBlank(message = "JWT secret key is required.")
    private String secretKey;

    @NotNull(message = "JWT access token expiration is required.")
    @Positive(message = "JWT access token expiration must be positive.")
    private Long accessTokenExpirationMillis;

    @NotNull(message = "JWT refresh token expiration is required.")
    @Positive(message = "JWT refresh token expiration must be positive.")
    private Long refreshTokenExpirationMillis;

    @NotNull(message = "JWT signup token expiration is required.")
    @Positive(message = "JWT signup token expiration must be positive.")
    private Long signupTokenExpirationMillis;

    @AssertTrue(message = "JWT secret key must be at least 32 bytes.")
    public boolean isSecretKeyAtLeast32Bytes() {
        return secretKey != null
                && secretKey.getBytes(StandardCharsets.UTF_8).length >= MIN_SECRET_LENGTH_BYTES;
    }
}
