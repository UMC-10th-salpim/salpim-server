package salpim.umc10thsalpim.domain.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;

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
    private static final int MIN_SECRET_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResDTO.TokenResult issueLoginTokens(Member member) {
        String accessToken = createMemberToken(
                member,
                TokenPurpose.ACCESS,
                jwtProperties.getAccessTokenExpirationMillis()
        );
        String refreshToken = createMemberToken(
                member,
                TokenPurpose.REFRESH,
                jwtProperties.getRefreshTokenExpirationMillis()
        );
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMillis()));

        RefreshToken savedRefreshToken = refreshTokenRepository.findByMember(member)
                .map(existingToken -> {
                    existingToken.updateToken(refreshToken, refreshTokenExpiredAt);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .member(member)
                        .token(refreshToken)
                        .expiredAt(refreshTokenExpiredAt)
                        .build());

        refreshTokenRepository.save(savedRefreshToken);

        return AuthResDTO.TokenResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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
