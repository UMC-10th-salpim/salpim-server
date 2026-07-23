package salpim.umc10thsalpim.domain.member.dto;

import jakarta.validation.constraints.*;
import salpim.umc10thsalpim.domain.member.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MemberReqDTO {

    public record UpdateProfile(
            @NotBlank
            String name,

            @NotNull
            @Past
            LocalDate birthDate,

            @NotNull
            Gender gender,

            @NotBlank
            String roadAddress,

            String detailAddress,

            @NotNull
            @DecimalMin(value = "-90.0")
            @DecimalMax(value = "90.0")
            @Digits(integer = 3, fraction = 7)
            BigDecimal latitude,

            @NotNull
            @DecimalMin(value = "-90.0")
            @DecimalMax(value = "90.0")
            @Digits(integer = 3, fraction = 7)
            BigDecimal longitude,

            @NotNull
            @Positive
            Long regionId
    ){}
}
