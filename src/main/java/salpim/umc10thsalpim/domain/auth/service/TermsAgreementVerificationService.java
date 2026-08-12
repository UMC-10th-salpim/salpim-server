package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.entity.TermsAgreementVerification;
import salpim.umc10thsalpim.domain.auth.entity.TermsAgreementVerificationItem;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.auth.repository.TermsAgreementVerificationRepository;
import salpim.umc10thsalpim.domain.term.dto.TermReqDTO;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;
import salpim.umc10thsalpim.domain.term.service.TermService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsAgreementVerificationService {

    private static final long AGREEMENT_EXPIRATION_MINUTES = 5L;

    private final TermsAgreementVerificationRepository termsAgreementVerificationRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final TermService termService;
    private final TermsAgreementHasher termsAgreementHasher;

    // 약관 동의 화면 '다음' 클릭 - 전화번호 인증 해시와 약관 동의 정보를 묶은 별개의 해시를 만들어 저장한다.
    @Transactional
    public void submitAgreement(String phoneNumber, List<TermReqDTO.AgreementItem> agreements) {
        phoneVerificationService.validateVerifiedPhoneNumber(phoneNumber);
        String phoneVerificationCodeHash = getSignupCodeHash(phoneNumber);

        Map<TermsVersion, Boolean> resolvedAgreements = termService.resolveAndValidateAgreements(agreements);
        String agreementHash = termsAgreementHasher.hash(
                phoneVerificationCodeHash,
                buildAgreementSignature(resolvedAgreements)
        );
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(AGREEMENT_EXPIRATION_MINUTES);

        TermsAgreementVerification verification = termsAgreementVerificationRepository
                .findByPhoneNumberForUpdate(phoneNumber)
                .map(existing -> {
                    existing.updateAgreement(agreementHash, expiredAt);
                    return existing;
                })
                .orElseGet(() -> TermsAgreementVerification.builder()
                        .phoneNumber(phoneNumber)
                        .agreementHash(agreementHash)
                        .expiredAt(expiredAt)
                        .build());

        resolvedAgreements.forEach((termsVersion, agreed) -> verification.addItem(
                TermsAgreementVerificationItem.builder()
                        .termsAgreementVerification(verification)
                        .termsVersion(termsVersion)
                        .agreed(agreed)
                        .build()
        ));

        termsAgreementVerificationRepository.save(verification);
    }

    // 회원가입 시 전화번호 인증 해시 + 약관 동의 해시를 함께 검증하고, 저장된 동의 항목을 반환한다.
    // 약관 동의 제출 이후 인증번호가 재발송되어 전화번호 인증 해시가 바뀌면 검증에 실패한다.
    @Transactional
    public List<TermsAgreementVerificationItem> validateAgreedTerms(String phoneNumber) {
        TermsAgreementVerification verification = termsAgreementVerificationRepository
                .findByPhoneNumberForUpdate(phoneNumber)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TERMS_AGREEMENT_NOT_SUBMITTED));

        if (verification.isExpired(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.TERMS_AGREEMENT_EXPIRED);
        }

        String currentCodeHash = getSignupCodeHash(phoneNumber);
        Map<TermsVersion, Boolean> storedAgreements = verification.getItems().stream()
                .collect(Collectors.toMap(
                        TermsAgreementVerificationItem::getTermsVersion,
                        TermsAgreementVerificationItem::getAgreed
                ));

        boolean matches = termsAgreementHasher.matches(
                currentCodeHash,
                buildAgreementSignature(storedAgreements),
                verification.getAgreementHash()
        );
        if (!matches) {
            throw new AuthException(AuthErrorCode.TERMS_AGREEMENT_VERIFICATION_FAILED);
        }

        return List.copyOf(verification.getItems());
    }

    @Transactional
    public void invalidate(String phoneNumber) {
        termsAgreementVerificationRepository.deleteByPhoneNumber(phoneNumber);
    }

    private String getSignupCodeHash(String phoneNumber) {
        PhoneVerification phoneVerification = phoneVerificationRepository
                .findByPhoneNumberAndPurpose(phoneNumber, PhoneVerificationPurpose.SIGNUP)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PHONE_NOT_VERIFIED));
        return phoneVerification.getCodeHash();
    }

    private String buildAgreementSignature(Map<TermsVersion, Boolean> agreements) {
        return agreements.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getId()))
                .map(entry -> entry.getKey().getId() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
    }
}
