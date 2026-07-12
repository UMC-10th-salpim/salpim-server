package salpim.umc10thsalpim.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthReqDTO {

    public record PhoneSend(
            @NotBlank(message = "전화번호는 필수입니다.")
            String phoneNumber
    ) {
    }

    public record PhoneVerify(
            @NotBlank(message = "전화번호는 필수입니다.")
            String phoneNumber,

            @NotBlank(message = "인증번호는 필수입니다.")
            String code
    ) {
    }
}
