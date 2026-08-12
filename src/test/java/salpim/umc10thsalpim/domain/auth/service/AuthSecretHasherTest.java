package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.auth.config.AuthSecretProperties;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSecretHasherTest {

    private AuthSecretHasher authSecretHasher;

    @BeforeEach
    void setUp() {
        AuthSecretProperties properties = new AuthSecretProperties();
        properties.setHmacKey("YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODg5YWJjZGVm");
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

        assertThat(hash).hasSize(43);
        assertThat(authSecretHasher.matchesRefreshToken("refresh-token", hash)).isTrue();
        assertThat(authSecretHasher.matchesRefreshToken("replayed-token", hash)).isFalse();
        assertThat(authSecretHasher.matchesRefreshToken(null, hash)).isFalse();
    }

    @Test
    void createsDifferentCredentialFingerprintWhenPasswordChanges() {
        String previousFingerprint = authSecretHasher.createCredentialFingerprint(
                SocialProvider.LOCAL,
                "previous-password-hash"
        );
        String currentFingerprint = authSecretHasher.createCredentialFingerprint(
                SocialProvider.LOCAL,
                "current-password-hash"
        );

        assertThat(previousFingerprint).isNotEqualTo(currentFingerprint);
        assertThat(authSecretHasher.matchesCredentialFingerprint(
                previousFingerprint,
                previousFingerprint
        )).isTrue();
        assertThat(authSecretHasher.matchesCredentialFingerprint(
                previousFingerprint,
                currentFingerprint
        )).isFalse();
    }
}
