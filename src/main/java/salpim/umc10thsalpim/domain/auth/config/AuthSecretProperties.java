package salpim.umc10thsalpim.domain.auth.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
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
@ConfigurationProperties(prefix = "auth.secret")
public class AuthSecretProperties {

    private static final int MIN_SECRET_LENGTH_BYTES = 32;

    @NotBlank(message = "Authentication HMAC key is required.")
    private String hmacKey;

    @AssertTrue(message = "Authentication HMAC key must be a Base64-encoded random value of at least 32 bytes.")
    public boolean isHmacKeySecure() {
        if (hmacKey == null) {
            return false;
        }
        try {
            byte[] decodedKey = getDecodedHmacKey();
            return decodedKey.length >= MIN_SECRET_LENGTH_BYTES
                    && !containsOnlyRepeatedByte(decodedKey);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public byte[] getDecodedHmacKey() {
        return Base64.getDecoder().decode(hmacKey);
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
