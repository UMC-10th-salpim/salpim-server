package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PasswordResetTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String PASSWORD_RESET_TOKEN = "password-reset-token";
    private static final String TOKEN_ID_HASH = "token-id-hash";

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthSecretHasher authSecretHasher;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Test
    void issuePasswordResetTokenReplacesExistingTokenForMember() {
        Member member = member();
        PasswordResetToken existingToken = PasswordResetToken.builder()
                .member(member)
                .tokenIdHash("previous-token-id-hash")
                .expiredAt(LocalDateTime.now().plusMinutes(1))
                .build();
        existingToken.consume(LocalDateTime.now());

        when(memberRepository.findByIdForUpdate(MEMBER_ID)).thenReturn(Optional.of(member));
        when(authSecretHasher.hashPasswordResetTokenId(anyString())).thenReturn(TOKEN_ID_HASH);
        when(jwtProperties.getPasswordResetTokenExpirationMillis()).thenReturn(300_000L);
        when(tokenService.issuePasswordResetToken(
                org.mockito.ArgumentMatchers.eq(member),
                anyString()
        )).thenReturn(PASSWORD_RESET_TOKEN);
        when(passwordResetTokenRepository.findByMemberId(MEMBER_ID))
                .thenReturn(Optional.of(existingToken));

        String result = passwordResetTokenService.issuePasswordResetToken(member);

        assertThat(result).isEqualTo(PASSWORD_RESET_TOKEN);
        assertThat(existingToken.getTokenIdHash()).isEqualTo(TOKEN_ID_HASH);
        assertThat(existingToken.getUsedAt()).isNull();
        assertThat(existingToken.getExpiredAt()).isAfter(LocalDateTime.now());
        ArgumentCaptor<String> tokenIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(authSecretHasher).hashPasswordResetTokenId(tokenIdCaptor.capture());
        verify(tokenService).issuePasswordResetToken(member, tokenIdCaptor.getValue());
        verify(passwordResetTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getUsablePasswordResetTokenForUpdateRejectsConsumedToken() {
        Member member = member();
        PasswordResetToken consumedToken = PasswordResetToken.builder()
                .member(member)
                .tokenIdHash(TOKEN_ID_HASH)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
        consumedToken.consume(LocalDateTime.now());
        TokenDTO.PasswordResetTokenClaims claims = claims();

        when(authSecretHasher.hashPasswordResetTokenId(claims.tokenId()))
                .thenReturn(TOKEN_ID_HASH);
        when(passwordResetTokenRepository.findByTokenIdHashForUpdate(TOKEN_ID_HASH))
                .thenReturn(Optional.of(consumedToken));

        assertThatThrownBy(() ->
                passwordResetTokenService.getUsablePasswordResetTokenForUpdate(claims))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));
    }

    @Test
    void getUsablePasswordResetTokenForUpdateRejectsExpiredToken() {
        Member member = member();
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .member(member)
                .tokenIdHash(TOKEN_ID_HASH)
                .expiredAt(LocalDateTime.now().minusSeconds(1))
                .build();
        TokenDTO.PasswordResetTokenClaims claims = claims();

        when(authSecretHasher.hashPasswordResetTokenId(claims.tokenId()))
                .thenReturn(TOKEN_ID_HASH);
        when(passwordResetTokenRepository.findByTokenIdHashForUpdate(TOKEN_ID_HASH))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() ->
                passwordResetTokenService.getUsablePasswordResetTokenForUpdate(claims))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));
    }

    @Test
    void getUsablePasswordResetTokenForUpdateRejectsTokenForDifferentMember() {
        Member tokenOwner = member();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .member(tokenOwner)
                .tokenIdHash(TOKEN_ID_HASH)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
        TokenDTO.PasswordResetTokenClaims claims = new TokenDTO.PasswordResetTokenClaims(
                TokenPurpose.PASSWORD_RESET,
                MEMBER_ID + 1,
                "token-id"
        );

        when(authSecretHasher.hashPasswordResetTokenId(claims.tokenId()))
                .thenReturn(TOKEN_ID_HASH);
        when(passwordResetTokenRepository.findByTokenIdHashForUpdate(TOKEN_ID_HASH))
                .thenReturn(Optional.of(passwordResetToken));

        assertThatThrownBy(() ->
                passwordResetTokenService.getUsablePasswordResetTokenForUpdate(claims))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));
    }

    private TokenDTO.PasswordResetTokenClaims claims() {
        return new TokenDTO.PasswordResetTokenClaims(
                TokenPurpose.PASSWORD_RESET,
                MEMBER_ID,
                "token-id"
        );
    }

    private Member member() {
        return Member.builder().id(MEMBER_ID).build();
    }
}
