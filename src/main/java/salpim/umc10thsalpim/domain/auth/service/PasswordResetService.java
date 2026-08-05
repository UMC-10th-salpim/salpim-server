package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public AuthResDTO.PasswordResetVerifyResult verifyRecoveryAnswer(
            AuthReqDTO.PasswordResetVerify request
    ) {
        Member member = memberRepository.findByPhoneNumber(
                normalizePhoneNumber(request.phoneNumber())
        ).orElseThrow(this::passwordResetVerificationFailed);

        validateRecoveryAnswer(member, request.recoveryAnswer());

        return new AuthResDTO.PasswordResetVerifyResult(
                tokenService.issuePasswordResetToken(member)
        );
    }


    private void validateRecoveryAnswer(Member member, String recoverAnswer) {
        if (member.getLoginType() != SocialProvider.LOCAL
            || member.getPasswordRecoveryAnswer() == null
            || !passwordEncoder.matches(
                    recoverAnswer.trim(), member.getPasswordRecoveryAnswer()))
        {
            throw passwordResetVerificationFailed();
        }
    }

    private AuthException passwordResetVerificationFailed() {
        return new AuthException(AuthErrorCode.PASSWORD_RESET_VERIFICATION_FAILED);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
