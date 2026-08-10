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

import java.util.Base64;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH_BYTES = 32;
    private static final String EXAMPLE_SECRET = "REPLACE_WITH_RANDOM_BASE64_SECRET";

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

    @NotNull(message = "JWT password reset token expiration is required.")
    @Positive(message = "JWT password reset token expiration must be positive.")
    private Long passwordResetTokenExpirationMillis;

    @AssertTrue(message = "JWT secret key must be a Base64-encoded random value of at least 32 bytes.")
    public boolean isSecretKeySecure() {
        if (secretKey == null
                || EXAMPLE_SECRET.equals(secretKey)
                || secretKey.startsWith("your_jwt_secret_key")) {
            return false;
        }
        try {
            byte[] decodedKey = getDecodedSecretKey();
            return decodedKey.length >= MIN_SECRET_LENGTH_BYTES
                    && !containsOnlyRepeatedByte(decodedKey);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public byte[] getDecodedSecretKey() {
        return Base64.getDecoder().decode(secretKey);
    }

    private boolean containsOnlyRepeatedByte(byte[] value) {
        if (value.length == 0) {
            return true;
        }
        byte first = value[0];
        for (byte current : value) {
            if (current != first) {
                return false;
            }
        }
        return true;
    }
}
