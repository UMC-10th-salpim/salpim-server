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

// 전화번호 인증 해시와 약관 동의 정보를 묶어 별도의 약관 동의 해시를 생성/검증한다.
// terms_agreement_verification 테이블에 저장되는 값 전용이라 AuthSecretHasher와 분리한다.
@Component
@RequiredArgsConstructor
public class TermsAgreementHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TERMS_AGREEMENT_CONTEXT = "terms-agreement-verification:";

    private final AuthSecretProperties authSecretProperties;

    public String hash(String phoneVerificationCodeHash, String agreementSignature) {
        return hmac(phoneVerificationCodeHash + ":" + agreementSignature);
    }

    public boolean matches(
            String phoneVerificationCodeHash,
            String agreementSignature,
            String expectedHash
    ) {
        if (phoneVerificationCodeHash == null || agreementSignature == null || expectedHash == null) {
            return false;
        }
        byte[] actual = hash(phoneVerificationCodeHash, agreementSignature).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    authSecretProperties.getDecodedHmacKey(),
                    HMAC_ALGORITHM
            ));
            byte[] digest = mac.doFinal((TERMS_AGREEMENT_CONTEXT + value).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to hash terms agreement verification.", exception);
        }
    }
}
