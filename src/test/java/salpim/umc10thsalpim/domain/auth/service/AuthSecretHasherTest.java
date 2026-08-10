package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSecretHasherTest {

    private AuthSecretHasher authSecretHasher;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("test-only-secret-key-that-is-longer-than-32-bytes");
        authSecretHasher = new AuthSecretHasher(properties);
    }

    @Test
    void hashesVerificationCodeWithoutPersistingPlaintext() {
        String hash = authSecretHasher.hashVerificationCode("123456");

        assertThat(hash).isNotEqualTo("123456");
        assertThat(authSecretHasher.matchesVerificationCode("123456", hash)).isTrue();
        assertThat(authSecretHasher.matchesVerificationCode("654321", hash)).isFalse();
    }

    @Test
    void separatesRefreshTokenAndVerificationCodeHashDomains() {
        String value = "same-input";

        assertThat(authSecretHasher.hashRefreshToken(value))
                .isNotEqualTo(authSecretHasher.hashVerificationCode(value));
    }

    @Test
    void comparesRefreshTokenWithStoredHash() {
        String hash = authSecretHasher.hashRefreshToken("refresh-token");

        assertThat(authSecretHasher.matchesRefreshToken("refresh-token", hash)).isTrue();
        assertThat(authSecretHasher.matchesRefreshToken("replayed-token", hash)).isFalse();
        assertThat(authSecretHasher.matchesRefreshToken(null, hash)).isFalse();
    }
}
