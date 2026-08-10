package salpim.umc10thsalpim.domain.auth.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void rejectsDocumentedPlaceholderEvenWhenItIsLongEnough() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("REPLACE_WITH_RANDOM_BASE64_SECRET");

        assertThat(properties.isSecretKeySecure()).isFalse();
    }

    @Test
    void acceptsBase64SecretWithExactlyThirtyTwoDecodedBytes() {
        JwtProperties properties = new JwtProperties();
        byte[] secret = new byte[32];
        for (int index = 0; index < secret.length; index++) {
            secret[index] = (byte) index;
        }
        properties.setSecretKey(Base64.getEncoder().encodeToString(secret));

        assertThat(properties.isSecretKeySecure()).isTrue();
    }

    @Test
    void rejectsRepeatedByteSecret() {
        JwtProperties properties = new JwtProperties();
        byte[] repeatedSecret = new byte[32];
        Arrays.fill(repeatedSecret, (byte) 'a');
        properties.setSecretKey(Base64.getEncoder().encodeToString(repeatedSecret));

        assertThat(properties.isSecretKeySecure()).isFalse();
    }
}
