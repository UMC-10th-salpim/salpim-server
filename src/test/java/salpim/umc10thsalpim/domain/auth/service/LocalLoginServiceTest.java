package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.enums.WordSize;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalLoginServiceTest {

    private static final String PHONE_NUMBER = "01031768867";
    private static final String RAW_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String CLIENT_IP = "203.0.113.10";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private LocalLoginService localLoginService;

    @Test
    void loginSucceedsWithLocalMember() {
        AuthReqDTO.LocalLogin request = new AuthReqDTO.LocalLogin("010-3176-8867", RAW_PASSWORD);
        Member member = localMember();
        AuthResDTO.TokenResult expectedToken = new AuthResDTO.TokenResult(
                "access-token",
                "refresh-token",
                WordSize.LARGE
        );

        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenService.issueLoginTokens(member)).thenReturn(expectedToken);

        AuthResDTO.TokenResult result = localLoginService.login(request, CLIENT_IP);

        assertThat(result).isEqualTo(expectedToken);
        assertThat(result.wordSize()).isEqualTo(WordSize.LARGE);
        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
        verify(loginAttemptService).clearPhoneFailures(PHONE_NUMBER);
    }

    @Test
    void loginFailsWhenMemberDoesNotExist() {
        AuthReqDTO.LocalLogin request = new AuthReqDTO.LocalLogin(PHONE_NUMBER, RAW_PASSWORD);
        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localLoginService.login(request, CLIENT_IP))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_LOGIN_CREDENTIALS));

        verify(loginAttemptService).recordFailure(PHONE_NUMBER, CLIENT_IP);
    }

    @Test
    void loginFailsWhenPasswordDoesNotMatch() {
        AuthReqDTO.LocalLogin request = new AuthReqDTO.LocalLogin(PHONE_NUMBER, RAW_PASSWORD);
        Member member = localMember();

        when(memberRepository.findByPhoneNumber(PHONE_NUMBER)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> localLoginService.login(request, CLIENT_IP))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_LOGIN_CREDENTIALS));

        verify(loginAttemptService).recordFailure(PHONE_NUMBER, CLIENT_IP);
    }

    @Test
    void loginStopsBeforeAuthenticationWhenAttemptsAreExceeded() {
        AuthReqDTO.LocalLogin request = new AuthReqDTO.LocalLogin(PHONE_NUMBER, RAW_PASSWORD);
        doThrow(new AuthException(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED))
                .when(loginAttemptService).validateAllowed(PHONE_NUMBER, CLIENT_IP);

        assertThatThrownBy(() -> localLoginService.login(request, CLIENT_IP))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED));

        verifyNoInteractions(memberRepository, passwordEncoder, tokenService);
    }

    private Member localMember() {
        return Member.builder()
                .loginType(SocialProvider.LOCAL)
                .phoneNumber(PHONE_NUMBER)
                .password(ENCODED_PASSWORD)
                .build();
    }
}
