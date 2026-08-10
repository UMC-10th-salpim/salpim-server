package salpim.umc10thsalpim.domain.auth.dto;

import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

public class TokenDTO {

    public record SignupTokenClaims(
            TokenPurpose purpose,
            SocialProvider provider,
            String providerId
    ) {
    }

    public record RefreshTokenClaims(
            TokenPurpose purpose,
            Long memberId
    ) {
    }

    public record PasswordResetTokenClaims(
            TokenPurpose purpose,
            Long memberId
    ){
    }
}
