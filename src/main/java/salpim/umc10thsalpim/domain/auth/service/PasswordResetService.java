package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final PasswordVerificationAttemptService passwordVerificationAttemptService;
    private final PasswordResetTokenService passwordResetTokenService;

    @Transactional
    public AuthResDTO.PasswordResetVerifyResult verifyRecoveryAnswer(
            AuthReqDTO.PasswordResetVerify request
    ) {
        String phoneNumber = normalizePhoneNumber(request.phoneNumber());

        passwordVerificationAttemptService.validateAttemptAllowed(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                phoneNumber
        );

        try{
            Member member = memberRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(this::passwordResetVerificationFailed);

            validateRecoveryAnswer(member, request.recoveryAnswer());

            passwordVerificationAttemptService.clearFailures(
                    PasswordVerificationPurpose.PASSWORD_RESET,
                    PasswordVerificationTargetType.PHONE_NUMBER,
                    phoneNumber
            );

            return new AuthResDTO.PasswordResetVerifyResult(
                    passwordResetTokenService.issuePasswordResetToken(member)
            );
        } catch (AuthException e) {
            passwordVerificationAttemptService.recordFailure(
                    PasswordVerificationPurpose.PASSWORD_RESET,
                    PasswordVerificationTargetType.PHONE_NUMBER,
                    phoneNumber
            );

            throw e;
        }
    }

    @Transactional
    public void resetPassword(AuthReqDTO.PasswordReset request) {
        TokenDTO.PasswordResetTokenClaims claims =
                tokenService.parsePasswordResetToken(request.passwordResetToken());

        Member member = memberRepository.findByIdForUpdate(claims.memberId())
                .orElseThrow(() ->
                        new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        PasswordResetToken passwordResetToken = passwordResetTokenService
                .getUsablePasswordResetTokenForUpdate(claims);

        if (member.getLoginType() != SocialProvider.LOCAL) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        validateNewPasswordIsDifferent(member, request.newPassword());

        passwordResetToken.consume(LocalDateTime.now());
        member.changePassword(passwordEncoder.encode(request.newPassword()));
        tokenService.invalidateMemberSession(member);
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

    private void validateNewPasswordIsDifferent(
            Member member,
            String newPassword
    ) {
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new MemberException(
                    MemberErrorCode.PASSWORD_SAME_AS_CURRENT
            );
        }
    }
}
