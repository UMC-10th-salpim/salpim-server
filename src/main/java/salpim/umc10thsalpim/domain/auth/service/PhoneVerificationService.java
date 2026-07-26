package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.converter.AuthConverter;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhoneVerificationService {

    private static final long VERIFICATION_EXPIRATION_MINUTES = 5L;
    private static final long VERIFICATION_RESEND_INTERVAL_SECONDS = 60L;
    private static final long PHONE_CHANGE_TOKEN_EXPIRATION_MINUTES = 10L;
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;
    private static final int PHONE_CHANGE_TOKEN_BYTE_LENGTH = 32;

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiscordWebhookNotifier discordWebhookNotifier;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        if (memberRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        validateResendInterval(
                normalizedPhoneNumber,
                PhoneVerificationPurpose.SIGNUP
        );

        sendVerificationCode(
                null,
                normalizedPhoneNumber,
                PhoneVerificationPurpose.SIGNUP
        );
    }

    @Transactional
    public void sendPhoneChangeVerificationCode(Long memberId, String phoneNumber) {
        Member member = getMemberOrThrow(memberId);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        if (memberRepository.existsByPhoneNumberAndIdNot(normalizedPhoneNumber, memberId)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        validateResendInterval(
                normalizedPhoneNumber,
                PhoneVerificationPurpose.PHONE_CHANGE
        );

        phoneVerificationRepository.deleteByPhoneNumberAndPurpose(
                normalizedPhoneNumber,
                PhoneVerificationPurpose.PHONE_CHANGE
        );
        phoneVerificationRepository.flush();

        sendVerificationCode(
                member,
                normalizedPhoneNumber,
                PhoneVerificationPurpose.PHONE_CHANGE
        );
    }

    @Transactional
    public AuthResDTO.PhoneVerifyResult verifyCode(String phoneNumber, String code) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository
                .findByPhoneNumberAndPurpose(
                        normalizedPhoneNumber,
                        PhoneVerificationPurpose.SIGNUP
                )
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE));

        validateVerificationCode(phoneVerification, code);
        phoneVerification.verify();

        return AuthConverter.toPhoneVerifyResult(true);
    }

    @Transactional
    public AuthResDTO.PhoneChangeVerifyResult verifyPhoneChangeCode(
            Long memberId,
            String phoneNumber,
            String code
    ) {
        Member member = getMemberOrThrow(memberId);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository
                .findByMemberAndPhoneNumberAndPurpose(
                        member,
                        normalizedPhoneNumber,
                        PhoneVerificationPurpose.PHONE_CHANGE
                )
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE));

        validateVerificationCode(phoneVerification, code);

        String phoneVerificationToken = generatePhoneChangeToken();
        LocalDateTime tokenExpiredAt = LocalDateTime.now()
                .plusMinutes(PHONE_CHANGE_TOKEN_EXPIRATION_MINUTES);

        phoneVerification.verifyAndIssueToken(
                passwordEncoder.encode(phoneVerificationToken),
                tokenExpiredAt
        );

        return AuthConverter.toPhoneChangeVerifyResult(phoneVerificationToken);
    }

    public void validateVerifiedPhoneNumber(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository
                .findByPhoneNumberAndPurpose(
                        normalizedPhoneNumber,
                        PhoneVerificationPurpose.SIGNUP
                )
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
        phoneVerificationRepository.deleteByPhoneNumberAndPurpose(
                normalizePhoneNumber(phoneNumber),
                PhoneVerificationPurpose.SIGNUP
        );
    }

    private void sendVerificationCode(
            Member member,
            String normalizedPhoneNumber,
            PhoneVerificationPurpose purpose
    ) {
        String code = generateVerificationCode();
        LocalDateTime sentAt = LocalDateTime.now();
        LocalDateTime expiredAt = sentAt
                .plusMinutes(VERIFICATION_EXPIRATION_MINUTES);

        PhoneVerification phoneVerification = findVerification(
                member,
                normalizedPhoneNumber,
                purpose
        ).map(existingVerification -> {
            existingVerification.updateCode(code, expiredAt, sentAt);
            return existingVerification;
        }).orElseGet(() -> AuthConverter.toPhoneVerification(
                member,
                normalizedPhoneNumber,
                purpose,
                code,
                expiredAt,
                sentAt
        ));

        try {
            phoneVerificationRepository.saveAndFlush(phoneVerification);
        } catch (DataIntegrityViolationException exception) {
            throw new AuthException(AuthErrorCode.PHONE_VERIFICATION_RESEND_TOO_SOON);
        }

        log.info(
                "[DEV] phone verification code. maskedPhoneNumber={}, code={}",
                maskPhoneNumber(normalizedPhoneNumber),
                code
        );
        discordWebhookNotifier.sendVerificationCode(
                maskPhoneNumber(normalizedPhoneNumber),
                code,
                purpose
        );
    }

    @Transactional
    public String validateAndConsumePhoneChangeToken(
            Member member,
            String phoneNumber,
            String phoneVerificationToken
    ) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);

        PhoneVerification phoneVerification = phoneVerificationRepository
                .findByMemberAndPhoneNumberAndPurpose(
                        member,
                        normalizedPhoneNumber,
                        PhoneVerificationPurpose.PHONE_CHANGE
                )
                .orElseThrow(() -> new AuthException(
                        AuthErrorCode.INVALID_PHONE_VERIFICATION_TOKEN
                ));

        if (!Boolean.TRUE.equals(phoneVerification.getVerified())
                || phoneVerification.getVerificationTokenHash() == null
                || phoneVerification.getTokenExpiredAt() == null
                || phoneVerification.getUsedAt() != null) {
            throw new AuthException(AuthErrorCode.INVALID_PHONE_VERIFICATION_TOKEN);
        }

        if (phoneVerification.getTokenExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_PHONE_VERIFICATION_TOKEN);
        }

        if (!passwordEncoder.matches(
                phoneVerificationToken,
                phoneVerification.getVerificationTokenHash()
        )) {
            throw new AuthException(AuthErrorCode.INVALID_PHONE_VERIFICATION_TOKEN);
        }

        phoneVerification.consumeVerificationToken();

        return normalizedPhoneNumber;
    }

    private Optional<PhoneVerification> findVerification(
            Member member,
            String phoneNumber,
            PhoneVerificationPurpose purpose
    ) {
        return switch (purpose) {
            case SIGNUP -> phoneVerificationRepository
                    .findByPhoneNumberAndPurpose(phoneNumber, purpose);
            case PHONE_CHANGE -> phoneVerificationRepository
                    .findByMemberAndPhoneNumberAndPurpose(member, phoneNumber, purpose);
        };
    }

    private void validateResendInterval(
            String phoneNumber,
            PhoneVerificationPurpose purpose
    ) {
        phoneVerificationRepository.findByPhoneNumberAndPurposeForUpdate(phoneNumber, purpose)
                .filter(phoneVerification -> phoneVerification.getSentAt() != null
                        && phoneVerification.getSentAt()
                                .plusSeconds(VERIFICATION_RESEND_INTERVAL_SECONDS)
                                .isAfter(LocalDateTime.now()))
                .ifPresent(phoneVerification -> {
                    throw new AuthException(AuthErrorCode.PHONE_VERIFICATION_RESEND_TOO_SOON);
                });
    }

    private void validateVerificationCode(
            PhoneVerification phoneVerification,
            String code
    ) {
        if (phoneVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        if (!phoneVerification.getCode().equals(code)) {
            throw new AuthException(AuthErrorCode.INVALID_VERIFICATION_CODE);
        }
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(VERIFICATION_CODE_BOUND));
    }

    private String generatePhoneChangeToken() {
        byte[] tokenBytes = new byte[PHONE_CHANGE_TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
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
