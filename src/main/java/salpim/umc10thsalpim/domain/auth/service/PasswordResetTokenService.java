package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.config.JwtProperties;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PasswordResetTokenRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final AuthSecretHasher authSecretHasher;
    private final JwtProperties jwtProperties;

    @Transactional
    public String issuePasswordResetToken(Member member) {
        Member lockedMember = memberRepository.findByIdForUpdate(member.getId())
                .orElseThrow(() ->
                        new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID)
                );

        String tokenId = UUID.randomUUID().toString();
        String tokenIdHash = authSecretHasher.hashPasswordResetTokenId(tokenId);
        LocalDateTime expiredAt = LocalDateTime.now()
                .plus(Duration.ofMillis(
                        jwtProperties.getPasswordResetTokenExpirationMillis()
                ));

        String passwordResetToken = tokenService.issuePasswordResetToken(
                lockedMember,
                tokenId
        );

        passwordResetTokenRepository.findByMemberId(lockedMember.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.replaceToken(
                                tokenIdHash,
                                expiredAt
                        ),
                        () -> passwordResetTokenRepository.save(
                                PasswordResetToken.builder()
                                        .member(lockedMember)
                                        .tokenIdHash(tokenIdHash)
                                        .expiredAt(expiredAt)
                                        .build()
                        )
                );

        return passwordResetToken;
    }

    @Transactional
    public PasswordResetToken getUsablePasswordResetTokenForUpdate(
            TokenDTO.PasswordResetTokenClaims claims
    ) {
        LocalDateTime now = LocalDateTime.now();
        String tokenIdHash = authSecretHasher.hashPasswordResetTokenId(
                claims.tokenId()
        );

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenIdHashForUpdate(tokenIdHash)
                .orElseThrow(() ->
                        new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        if (!passwordResetToken.getMember().getId().equals(claims.memberId())
                || !passwordResetToken.isUsableAt(now)) {
            throw new AuthException(AuthErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        return passwordResetToken;
    }
}
