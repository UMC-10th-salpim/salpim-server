package salpim.umc10thsalpim.domain.member.dto;

import jakarta.validation.constraints.*;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.PasswordVerificationMethod;
import salpim.umc10thsalpim.domain.member.enums.WordSize;

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
            @DecimalMin(value = "-180.0")
            @DecimalMax(value = "180.0")
            @Digits(integer = 3, fraction = 7)
            BigDecimal longitude,

            @NotNull
            @Positive
            Long regionId,

            @Pattern(regexp = "^[0-9-]+$")
            String phoneNumber,

            String phoneVerificationToken
    ){}

    public record UpdateWordSize(
            @NotNull
            WordSize wordSize
    ){}

    public record VerifyCurrentPassword(
            @NotBlank
            @Pattern(regexp = "^\\d{6}$")
            String currentPassword
    ) {}

    public record VerifyRecoveryAnswer(
            @NotBlank
            @Size(max = 255)
            String recoveryAnswer
    ) {}

    public record ChangePassword(
            @NotNull
            PasswordVerificationMethod verificationMethod,

            @Pattern(regexp = "^\\d{6}$")
            String currentPassword,

            @Size(max = 255)
            String recoveryAnswer,

            @NotBlank
            @Pattern(regexp = "^\\d{6}$")
            String newPassword
    ) {}
}
