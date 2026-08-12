package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.entity.TermsAgreementVerificationItem;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.term.converter.TermConverter;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalSignupService {

    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final TermsAgreementVerificationService termsAgreementVerificationService;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final SignupValidationService signupValidationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthReqDTO.LocalSignup request) {
        String normalizedPhoneNumber = signupValidationService.normalizePhoneNumber(request.phoneNumber());

        validateLocalRequiredFields(request);
        signupValidationService.validateDuplicatePhoneNumber(normalizedPhoneNumber);
        phoneVerificationService.validateVerifiedPhoneNumber(normalizedPhoneNumber);
        List<TermsAgreementVerificationItem> agreedTerms =
                termsAgreementVerificationService.validateAgreedTerms(normalizedPhoneNumber);
        Region region = signupValidationService.findLeafRegion(request.regionId());

        String encodedPassword = passwordEncoder.encode(request.password());
        String encodedPasswordRecoveryAnswer = passwordEncoder.encode(
                request.passwordAnswer().trim()
        );
        Member member;
        try {
            member = memberRepository.saveAndFlush(
                    MemberConverter.toLocalMember(
                            request,
                            normalizedPhoneNumber,
                            encodedPassword,
                            encodedPasswordRecoveryAnswer,
                            region
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            if (MemberConstraintViolationClassifier.isViolationOf(
                    exception,
                    MemberConstraintViolationClassifier.PHONE_NUMBER_CONSTRAINT
            )) {
                throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            throw exception;
        }
        agreedTerms.forEach(item -> memberTermAgreementRepository.save(
                TermConverter.toMemberAgreement(member, item.getTermsVersion(), item.getAgreed())
        ));
        phoneVerificationService.deleteVerification(normalizedPhoneNumber);
        termsAgreementVerificationService.invalidate(normalizedPhoneNumber);
    }

    private void validateLocalRequiredFields(AuthReqDTO.LocalSignup request) {
        if (!StringUtils.hasText(request.password())) {
            throw new MemberException(MemberErrorCode.REQUIRED_LOCAL_PASSWORD);
        }
        if (!StringUtils.hasText(request.passwordAnswer())) {
            throw new MemberException(MemberErrorCode.REQUIRED_PASSWORD_RECOVERY_ANSWER);
        }
    }
}
