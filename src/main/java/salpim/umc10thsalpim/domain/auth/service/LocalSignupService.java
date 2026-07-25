package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalSignupService {

    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final SignupValidationService signupValidationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthReqDTO.LocalSignup request) {
        String normalizedPhoneNumber = signupValidationService.normalizePhoneNumber(request.phoneNumber());

        validateLocalRequiredFields(request);
        signupValidationService.validateDuplicatePhoneNumber(normalizedPhoneNumber);
        phoneVerificationService.validateVerifiedPhoneNumber(normalizedPhoneNumber);
        Region region = signupValidationService.findLeafRegion(request.regionId());

        String encodedPassword = passwordEncoder.encode(request.password());
        String encodedPasswordRecoveryAnswer = passwordEncoder.encode(
                request.passwordAnswer().trim()
        );
        memberRepository.save(
                MemberConverter.toLocalMember(
                        request,
                        normalizedPhoneNumber,
                        encodedPassword,
                        encodedPasswordRecoveryAnswer,
                        region
                )
        );
        phoneVerificationService.deleteVerification(normalizedPhoneNumber);
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
