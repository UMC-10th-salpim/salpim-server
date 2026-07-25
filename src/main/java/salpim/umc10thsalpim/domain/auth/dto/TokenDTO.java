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
}
