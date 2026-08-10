package salpim.umc10thsalpim.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoOAuthResDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Token(
            @JsonProperty("access_token")
            String accessToken,

            @JsonProperty("token_type")
            String tokenType,

            @JsonProperty("refresh_token")
            String refreshToken,

            @JsonProperty("expires_in")
            Integer expiresIn,

            String scope,

            @JsonProperty("refresh_token_expires_in")
            Integer refreshTokenExpiresIn
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(
            Long id,

            @JsonProperty("kakao_account")
            KakaoAccount kakaoAccount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(
            String name,
            String gender,
            String birthyear,
            String birthday,

            @JsonProperty("birthday_type")
            String birthdayType,

            @JsonProperty("phone_number")
            String phoneNumber,

            @JsonProperty("phone_number_needs_agreement")
            Boolean phoneNumberNeedsAgreement,

            Profile profile
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(
            String nickname
    ) {
    }
}
