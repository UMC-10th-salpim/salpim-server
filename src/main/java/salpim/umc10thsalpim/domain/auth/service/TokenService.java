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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private static final String CLAIM_PURPOSE = "purpose";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_PROVIDER_ID = "providerId";
    private static final String CLAIM_PROVIDER_PHONE_NUMBER = "providerPhoneNumber";
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
        String refreshToken = createRefreshToken(lockedMember);
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
    public AuthResDTO.TokenResult reissueLoginTokens(String refreshToken) {
        TokenDTO.RefreshTokenClaims claims = parseRefreshToken(refreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository
                .findByMemberIdForUpdate(claims.memberId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        LocalDateTime now = LocalDateTime.now();
        if (!savedRefreshToken.getExpiredAt().isAfter(now)) {
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        if (!authSecretHasher.matchesRefreshToken(
                refreshToken,
                savedRefreshToken.getTokenHash()
        )) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = savedRefreshToken.getMember();
        String accessToken = createMemberToken(
                member,
                TokenPurpose.ACCESS,
                jwtProperties.getAccessTokenExpirationMillis()
        );
        String rotatedRefreshToken = createRefreshToken(member);
        LocalDateTime rotatedRefreshTokenExpiredAt = now
                .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));

        savedRefreshToken.updateTokenHash(
                authSecretHasher.hashRefreshToken(rotatedRefreshToken),
                rotatedRefreshTokenExpiredAt
        );
        refreshTokenRepository.save(savedRefreshToken);

        return AuthResDTO.TokenResult.builder()
                .accessToken(accessToken)
                .refreshToken(rotatedRefreshToken)
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
                .phoneVerificationRequired(false)
                .build();
    }

    public AuthResDTO.KakaoLoginResult issueSignupRequiredToken(
            SocialProvider provider,
            String providerId,
            String providerPhoneNumber
    ) {
        String signupToken = createSignupToken(provider, providerId, providerPhoneNumber);
        boolean phoneVerificationRequired = !StringUtils.hasText(providerPhoneNumber);
        return AuthResDTO.KakaoLoginResult.builder()
                .isNewMember(true)
                .nextStep(NextStep.SIGNUP_REQUIRED)
                .signupToken(signupToken)
                .phoneNumber(providerPhoneNumber)
                .phoneVerificationRequired(phoneVerificationRequired)
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
                    claims.get(CLAIM_PROVIDER_ID, String.class),
                    claims.get(CLAIM_PROVIDER_PHONE_NUMBER, String.class)
            );
        } catch (AuthException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_EXPIRED);
        } catch (IllegalArgumentException | JwtException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_INVALID);
        }
    }

    public TokenDTO.RefreshTokenClaims parseRefreshToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TokenPurpose purpose = TokenPurpose.valueOf(
                    claims.get(CLAIM_PURPOSE, String.class)
            );
            if (purpose != TokenPurpose.REFRESH) {
                throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
            return new TokenDTO.RefreshTokenClaims(
                    purpose,
                    Long.parseLong(claims.getSubject())
            );
        } catch (AuthException exception) {
            throw exception;
        } catch (ExpiredJwtException exception) {
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (RuntimeException exception) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public String issuePasswordResetToken(Member member){
        return createMemberToken(
                member,
                TokenPurpose.PASSWORD_RESET,
                jwtProperties.getPasswordResetTokenExpirationMillis()
        );
    }

    public TokenDTO.PasswordResetTokenClaims parsePasswordResetToken(String token){
        try{
            var claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TokenPurpose purpose = TokenPurpose.valueOf(
                    claims.get(CLAIM_PURPOSE, String.class)
            );

            if (purpose != TokenPurpose.PASSWORD_RESET) {
                throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_TYPE_INVALID);
            }

            return new TokenDTO.PasswordResetTokenClaims(
                    purpose,
                    Long.parseLong(claims.getSubject())
            );
        } catch (AuthException e) {
            throw e;
        } catch (ExpiredJwtException e){
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        } catch (IllegalArgumentException | JwtException e) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
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

    private String createRefreshToken(Member member) {
        Date now = new Date();
        Date expiredAt = new Date(
                now.getTime() + jwtProperties.getRefreshTokenExpirationMillis()
        );

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(member.getId()))
                .claim(CLAIM_PURPOSE, TokenPurpose.REFRESH.name())
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(getSecretKey())
                .compact();
    }

    private String createSignupToken(
            SocialProvider provider,
            String providerId,
            String providerPhoneNumber
    ) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + jwtProperties.getSignupTokenExpirationMillis());

        var tokenBuilder = Jwts.builder()
                .claim(CLAIM_PURPOSE, TokenPurpose.SIGNUP.name())
                .claim(CLAIM_PROVIDER, provider.name())
                .claim(CLAIM_PROVIDER_ID, providerId)
                .issuedAt(now)
                .expiration(expiredAt);
        if (StringUtils.hasText(providerPhoneNumber)) {
            tokenBuilder.claim(CLAIM_PROVIDER_PHONE_NUMBER, providerPhoneNumber);
        }
        return tokenBuilder.signWith(getSecretKey()).compact();
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
