package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String PHONE_NUMBER = "01012345678";
    private static final String RECOVERY_ANSWER = "spring";
    private static final String ENCODED_RECOVERY_ANSWER = "encoded-recovery-answer";
    private static final String PASSWORD_RESET_TOKEN = "password-reset-token";
    private static final String TOKEN_ID = "token-id";
    private static final String NEW_PASSWORD = "123456";
    private static final String ENCODED_NEW_PASSWORD = "encoded-new-password";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordVerificationAttemptService passwordVerificationAttemptService;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void verifyRecoveryAnswerIssuesPasswordResetToken() {
        Member member = localMember();
        AuthReqDTO.PasswordResetVerify request = new AuthReqDTO.PasswordResetVerify(
                "010-1234-5678",
                RECOVERY_ANSWER
        );

        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(RECOVERY_ANSWER, ENCODED_RECOVERY_ANSWER)).thenReturn(true);
        when(passwordResetTokenService.issuePasswordResetToken(member))
                .thenReturn(PASSWORD_RESET_TOKEN);

        AuthResDTO.PasswordResetVerifyResult result =
                passwordResetService.verifyRecoveryAnswer(request);

        assertThat(result.passwordResetToken()).isEqualTo(PASSWORD_RESET_TOKEN);
        verify(passwordEncoder).matches(RECOVERY_ANSWER, ENCODED_RECOVERY_ANSWER);
        verify(passwordResetTokenService).issuePasswordResetToken(member);
        verify(passwordVerificationAttemptService).validateAttemptAllowed(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        );
        verify(passwordVerificationAttemptService).clearFailures(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        );
    }

    @Test
    void verifyRecoveryAnswerFailsWhenMemberDoesNotExist() {
        AuthReqDTO.PasswordResetVerify request = new AuthReqDTO.PasswordResetVerify(
                PHONE_NUMBER,
                RECOVERY_ANSWER
        );
        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.verifyRecoveryAnswer(request))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_VERIFICATION_FAILED));

        verifyNoInteractions(passwordEncoder, tokenService, passwordResetTokenService);
        verify(passwordVerificationAttemptService).recordFailure(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        );
    }

    @Test
    void verifyRecoveryAnswerFailsWhenAnswerDoesNotMatch() {
        Member member = localMember();
        AuthReqDTO.PasswordResetVerify request = new AuthReqDTO.PasswordResetVerify(
                PHONE_NUMBER,
                RECOVERY_ANSWER
        );
        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(RECOVERY_ANSWER, ENCODED_RECOVERY_ANSWER)).thenReturn(false);

        assertThatThrownBy(() -> passwordResetService.verifyRecoveryAnswer(request))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_VERIFICATION_FAILED));

        verify(passwordResetTokenService, never()).issuePasswordResetToken(member);
        verify(passwordVerificationAttemptService).recordFailure(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        );
    }

    @Test
    void resetPasswordConsumesTokenAndChangesPassword() {
        Member member = localMember();
        PasswordResetToken storedToken = activeToken(member);
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset(
                PASSWORD_RESET_TOKEN,
                NEW_PASSWORD
        );
        TokenDTO.PasswordResetTokenClaims claims = claims();

        when(tokenService.parsePasswordResetToken(PASSWORD_RESET_TOKEN)).thenReturn(claims);
        when(memberRepository.findByIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(member));
        when(passwordResetTokenService.getUsablePasswordResetTokenForUpdate(claims))
                .thenReturn(storedToken);
        when(passwordEncoder.matches(NEW_PASSWORD, member.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        passwordResetService.resetPassword(request);

        assertThat(storedToken.getUsedAt()).isNotNull();
        assertThat(member.getPassword()).isEqualTo(ENCODED_NEW_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(tokenService).invalidateMemberSession(member);
    }

    @Test
    void resetPasswordKeepsTokenWhenNewPasswordMatchesCurrentPassword() {
        Member member = localMember();
        PasswordResetToken storedToken = activeToken(member);
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset(
                PASSWORD_RESET_TOKEN,
                NEW_PASSWORD
        );
        TokenDTO.PasswordResetTokenClaims claims = claims();

        when(tokenService.parsePasswordResetToken(PASSWORD_RESET_TOKEN)).thenReturn(claims);
        when(memberRepository.findByIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(member));
        when(passwordResetTokenService.getUsablePasswordResetTokenForUpdate(claims))
                .thenReturn(storedToken);
        when(passwordEncoder.matches(NEW_PASSWORD, member.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.PASSWORD_SAME_AS_CURRENT));

        assertThat(storedToken.getUsedAt()).isNull();
        verify(passwordEncoder, never()).encode(NEW_PASSWORD);
        verify(tokenService, never()).invalidateMemberSession(member);
    }

    @Test
    void resetPasswordFailsWhenJwtIsInvalid() {
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset(
                PASSWORD_RESET_TOKEN,
                NEW_PASSWORD
        );
        when(tokenService.parsePasswordResetToken(PASSWORD_RESET_TOKEN))
                .thenThrow(new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        verifyNoInteractions(memberRepository, passwordEncoder, passwordResetTokenService);
    }

    private TokenDTO.PasswordResetTokenClaims claims() {
        return new TokenDTO.PasswordResetTokenClaims(
                TokenPurpose.PASSWORD_RESET,
                MEMBER_ID,
                TOKEN_ID
        );
    }

    private PasswordResetToken activeToken(Member member) {
        return PasswordResetToken.builder()
                .member(member)
                .tokenIdHash("token-id-hash")
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    private Member localMember() {
        return Member.builder()
                .id(MEMBER_ID)
                .loginType(SocialProvider.LOCAL)
                .phoneNumber(PHONE_NUMBER)
                .password("encoded-password")
                .passwordRecoveryAnswer(ENCODED_RECOVERY_ANSWER)
                .build();
    }
}
