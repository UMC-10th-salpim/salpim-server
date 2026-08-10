package salpim.umc10thsalpim.domain.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String SECRET_KEY = "password-reset-test-secret-key-must-be-at-least-32-bytes";

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthSecretHasher authSecretHasher;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtProperties.setAccessTokenExpirationMillis(3_600_000L);
        jwtProperties.setRefreshTokenExpirationMillis(1_209_600_000L);
        jwtProperties.setSignupTokenExpirationMillis(600_000L);
        jwtProperties.setPasswordResetTokenExpirationMillis(300_000L);

        tokenService = new TokenService(
                jwtProperties,
                refreshTokenRepository,
                memberRepository,
                authSecretHasher
        );
    }

    @Test
    void parsesIssuedPasswordResetToken() {
        Member member = Member.builder().id(MEMBER_ID).build();

        String token = tokenService.issuePasswordResetToken(member);

        TokenDTO.PasswordResetTokenClaims claims = tokenService.parsePasswordResetToken(token);

        assertThat(claims.purpose()).isEqualTo(TokenPurpose.PASSWORD_RESET);
        assertThat(claims.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    void rejectsTokenWithDifferentPurpose() {
        String token = createToken(TokenPurpose.ACCESS, new Date(System.currentTimeMillis() + 60_000L));

        assertThatThrownBy(() -> tokenService.parsePasswordResetToken(token))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_TYPE_INVALID));
    }

    @Test
    void rejectsExpiredPasswordResetToken() {
        String token = createToken(TokenPurpose.PASSWORD_RESET, new Date(System.currentTimeMillis() - 60_000L));

        assertThatThrownBy(() -> tokenService.parsePasswordResetToken(token))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.PASSWORD_RESET_TOKEN_EXPIRED));
    }

    @Test
    void reissuesAccessTokenAndRotatesRefreshToken() {
        String refreshToken = createToken(
                TokenPurpose.REFRESH,
                new Date(System.currentTimeMillis() + 60_000L)
        );
        Member member = Member.builder().id(MEMBER_ID).build();
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .member(member)
                .tokenHash("stored-refresh-token-hash")
                .expiredAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(refreshTokenRepository.findByMemberIdForUpdate(MEMBER_ID))
                .thenReturn(java.util.Optional.of(savedRefreshToken));
        when(authSecretHasher.matchesRefreshToken(
                refreshToken,
                "stored-refresh-token-hash"
        )).thenReturn(true);
        when(authSecretHasher.hashRefreshToken(anyString()))
                .thenReturn("rotated-refresh-token-hash");

        AuthResDTO.TokenResult result = tokenService.reissueLoginTokens(refreshToken);

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank().isNotEqualTo(refreshToken);
        assertThat(tokenService.validateAccessTokenAndGetMemberId(result.accessToken()))
                .isEqualTo(MEMBER_ID);
        assertThat(tokenService.parseRefreshToken(result.refreshToken()).memberId())
                .isEqualTo(MEMBER_ID);
        assertThat(savedRefreshToken.getTokenHash()).isEqualTo("rotated-refresh-token-hash");
        verify(refreshTokenRepository).save(savedRefreshToken);
    }

    @Test
    void rejectsRefreshTokenWhenStoredHashDoesNotMatch() {
        String refreshToken = createToken(
                TokenPurpose.REFRESH,
                new Date(System.currentTimeMillis() + 60_000L)
        );
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .member(Member.builder().id(MEMBER_ID).build())
                .tokenHash("rotated-refresh-token-hash")
                .expiredAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(refreshTokenRepository.findByMemberIdForUpdate(MEMBER_ID))
                .thenReturn(java.util.Optional.of(savedRefreshToken));
        when(authSecretHasher.matchesRefreshToken(
                refreshToken,
                "rotated-refresh-token-hash"
        )).thenReturn(false);

        assertThatThrownBy(() -> tokenService.reissueLoginTokens(refreshToken))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));
        verify(refreshTokenRepository, never()).save(savedRefreshToken);
    }

    @Test
    void rejectsRefreshTokenWhenStoredExpirationHasPassed() {
        String refreshToken = createToken(
                TokenPurpose.REFRESH,
                new Date(System.currentTimeMillis() + 60_000L)
        );
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .member(Member.builder().id(MEMBER_ID).build())
                .tokenHash("stored-refresh-token-hash")
                .expiredAt(LocalDateTime.now().minusSeconds(1))
                .build();

        when(refreshTokenRepository.findByMemberIdForUpdate(MEMBER_ID))
                .thenReturn(java.util.Optional.of(savedRefreshToken));

        assertThatThrownBy(() -> tokenService.reissueLoginTokens(refreshToken))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN));
        verify(authSecretHasher, never()).matchesRefreshToken(anyString(), anyString());
    }

    @Test
    void rejectsAccessTokenForReissue() {
        String accessToken = createToken(
                TokenPurpose.ACCESS,
                new Date(System.currentTimeMillis() + 60_000L)
        );

        assertThatThrownBy(() -> tokenService.reissueLoginTokens(accessToken))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));
        verify(refreshTokenRepository, never()).findByMemberIdForUpdate(MEMBER_ID);
    }

    private String createToken(TokenPurpose purpose, Date expiration) {
        return Jwts.builder()
                .subject(String.valueOf(MEMBER_ID))
                .claim("purpose", purpose.name())
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(secretKey())
                .compact();
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
