package salpim.umc10thsalpim.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import salpim.umc10thsalpim.domain.auth.enums.NextStep;
import salpim.umc10thsalpim.domain.member.enums.WordSize;

public class AuthResDTO {

    @Builder
    public record TokenResult(
            String accessToken,
            String refreshToken,
            WordSize wordSize
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
            String signupToken,
            String phoneNumber,
            Boolean phoneVerificationRequired,
            WordSize wordSize
    ) {
    }

    @Builder
    public record PhoneChangeVerifyResult(
            String phoneVerificationToken
    ) {
    }

    @Builder
    public record PasswordResetVerifyResult(
            String passwordResetToken
    ){
    }
}
