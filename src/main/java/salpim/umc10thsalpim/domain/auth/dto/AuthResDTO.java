package salpim.umc10thsalpim.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import salpim.umc10thsalpim.domain.auth.enums.NextStep;

public class AuthResDTO {

    @Builder
    public record TokenResult(
            String accessToken,
            String refreshToken
    ) {
    }

    @Builder
    public record PhoneVerifyResult(
            Boolean verified
    ) {
    }

    @Builder
    public record GeocodeResult(
            String roadAddress,
            Double latitude,
            Double longitude
    ) {
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record KakaoLoginResult(
            Boolean isNewMember,
            NextStep nextStep,
            String accessToken,
            String refreshToken,
            String signupToken
    ) {
    }

    @Builder
    public record PhoneChangeVerifyResult(
            String phoneVerificationToken
    ) {
    }
}
