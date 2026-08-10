package salpim.umc10thsalpim.domain.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
