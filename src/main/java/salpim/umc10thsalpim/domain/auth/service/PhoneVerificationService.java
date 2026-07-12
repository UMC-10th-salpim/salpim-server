package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.converter.AuthConverter;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.member.exception.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhoneVerificationService {

    private static final long VERIFICATION_EXPIRATION_MINUTES = 5L;
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final MemberRepository memberRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        if (memberRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        String code = generateVerificationCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(VERIFICATION_EXPIRATION_MINUTES);

        PhoneVerification phoneVerification = phoneVerificationRepository.findByPhoneNumber(normalizedPhoneNumber)
                .map(existingVerification -> {
                    existingVerification.updateCode(code, expiredAt);
                    return existingVerification;
                })
                .orElseGet(() -> AuthConverter.toPhoneVerification(normalizedPhoneNumber, code, expiredAt));

        phoneVerificationRepository.save(phoneVerification);
        log.info("[DEV] phone verification code. maskedPhoneNumber={}, code={}", maskPhoneNumber(normalizedPhoneNumber), code);
    }

    @Transactional
    public AuthResDTO.PhoneVerifyResult verifyCode(String phoneNumber, String code) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository.findByPhoneNumber(normalizedPhoneNumber)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE));

        if (phoneVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        if (!phoneVerification.getCode().equals(code)) {
            throw new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }

        phoneVerification.verify();
        return AuthConverter.toPhoneVerifyResult(true);
    }

    public void validateVerifiedPhoneNumber(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository.findByPhoneNumber(normalizedPhoneNumber)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PHONE_NOT_VERIFIED));

        if (!Boolean.TRUE.equals(phoneVerification.getVerified())) {
            throw new AuthException(AuthErrorCode.PHONE_NOT_VERIFIED);
        }

        if (phoneVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_VERIFICATION_CODE);
        }
    }

    @Transactional
    public void deleteVerification(String phoneNumber) {
        phoneVerificationRepository.deleteByPhoneNumber(normalizePhoneNumber(phoneNumber));
    }

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(VERIFICATION_CODE_BOUND));
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
