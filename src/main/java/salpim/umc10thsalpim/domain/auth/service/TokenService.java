package salpim.umc10thsalpim.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.auth.enums.NextStep;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private static final String CLAIM_PURPOSE = "purpose";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_PROVIDER_ID = "providerId";
    private static final int MIN_SECRET_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final AuthSecretHasher authSecretHasher;

    @Transactional
    public AuthResDTO.TokenResult issueLoginTokens(Member member) {
        Member lockedMember = memberRepository.findByIdForUpdate(member.getId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_MEMBER_NOT_FOUND));

        String accessToken = createMemberToken(
                lockedMember,
                TokenPurpose.ACCESS,
                jwtProperties.getAccessTokenExpirationMillis()
        );
        String refreshToken = createMemberToken(
                lockedMember,
                TokenPurpose.REFRESH,
                jwtProperties.getRefreshTokenExpirationMillis()
        );
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));
        String refreshTokenHash = authSecretHasher.hashRefreshToken(refreshToken);

        RefreshToken savedRefreshToken = refreshTokenRepository.findByMember(lockedMember)
                .map(existingToken -> {
                    existingToken.updateTokenHash(refreshTokenHash, refreshTokenExpiredAt);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .member(lockedMember)
                        .tokenHash(refreshTokenHash)
                        .expiredAt(refreshTokenExpiredAt)
                        .build());

        refreshTokenRepository.save(savedRefreshToken);

        return AuthResDTO.TokenResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResDTO.KakaoLoginResult issueKakaoLoginCompleteTokens(Member member) {
        AuthResDTO.TokenResult tokenResult = issueLoginTokens(member);
        return AuthResDTO.KakaoLoginResult.builder()
                .isNewMember(false)
                .nextStep(NextStep.LOGIN_COMPLETE)
                .accessToken(tokenResult.accessToken())
                .refreshToken(tokenResult.refreshToken())
                .build();
    }

    public AuthResDTO.KakaoLoginResult issueSignupRequiredToken(SocialProvider provider, String providerId) {
        String signupToken = createSignupToken(provider, providerId);
        return AuthResDTO.KakaoLoginResult.builder()
                .isNewMember(true)
                .nextStep(NextStep.SIGNUP_REQUIRED)
                .signupToken(signupToken)
                .build();
    }

    public TokenDTO.SignupTokenClaims parseSignupToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TokenPurpose purpose = TokenPurpose.valueOf(claims.get(CLAIM_PURPOSE, String.class));
            if (purpose != TokenPurpose.SIGNUP) {
                throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_TYPE_INVALID);
            }

            return new TokenDTO.SignupTokenClaims(
                    purpose,
                    SocialProvider.valueOf(claims.get(CLAIM_PROVIDER, String.class)),
                    claims.get(CLAIM_PROVIDER_ID, String.class)
            );
        } catch (AuthException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_EXPIRED);
        } catch (IllegalArgumentException | JwtException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_INVALID);
        }
    }

    private String createMemberToken(Member member, TokenPurpose purpose, Long expirationMillis) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim(CLAIM_PURPOSE, purpose.name())
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(getSecretKey())
                .compact();
    }

    private String createSignupToken(SocialProvider provider, String providerId) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + jwtProperties.getSignupTokenExpirationMillis());

        return Jwts.builder()
                .claim(CLAIM_PURPOSE, TokenPurpose.SIGNUP.name())
                .claim(CLAIM_PROVIDER, provider.name())
                .claim(CLAIM_PROVIDER_ID, providerId)
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(getSecretKey())
                .compact();
    }

    public Long validateAccessTokenAndGetMemberId(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!Objects.equals(TokenPurpose.ACCESS.name(), claims.get(CLAIM_PURPOSE, String.class))) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }
            return Long.parseLong(claims.getSubject());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private SecretKey getSecretKey() {
        if (!StringUtils.hasText(jwtProperties.getSecretKey())
                || jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
