package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.auth.config.AuthSecretProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AuthSecretHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String OTP_CONTEXT = "phone-verification:";
    private static final String REFRESH_TOKEN_CONTEXT = "refresh-token:";

    private final AuthSecretProperties authSecretProperties;

    public String hashVerificationCode(String code) {
        return hash(OTP_CONTEXT, code);
    }

    public boolean matchesVerificationCode(String code, String expectedHash) {
        if (code == null || expectedHash == null) {
            return false;
        }
        byte[] actual = hashVerificationCode(code).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    public String hashRefreshToken(String refreshToken) {
        return hash(REFRESH_TOKEN_CONTEXT, refreshToken);
    }

    public boolean matchesRefreshToken(String refreshToken, String expectedHash) {
        if (refreshToken == null || expectedHash == null) {
            return false;
        }
        byte[] actual = hashRefreshToken(refreshToken).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private String hash(String context, String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    authSecretProperties.getDecodedHmacKey(),
                    HMAC_ALGORITHM
            ));
            byte[] digest = mac.doFinal((context + value).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to hash authentication secret.", exception);
        }
    }
}
