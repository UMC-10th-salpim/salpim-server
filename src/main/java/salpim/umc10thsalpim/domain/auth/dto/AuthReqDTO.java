package salpim.umc10thsalpim.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import salpim.umc10thsalpim.domain.member.enums.Gender;

import java.time.LocalDate;

public class AuthReqDTO {

    public record LocalLogin(
            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = "^[0-9-]+$", message = "phoneNumber can contain only numbers and hyphens.")
            String phoneNumber,

            @Schema(example = "qwer1234")
            @NotBlank(message = "password is required.")
            String password
    ) {
    }

    public record PhoneSend(
            @NotBlank(message = "phoneNumber is required.")
            String phoneNumber
    ) {
    }

    public record PhoneVerify(
            @NotBlank(message = "phoneNumber is required.")
            String phoneNumber,

            @NotBlank(message = "code is required.")
            String code
    ) {
    }

    public record Geocode(
            @NotBlank(message = "roadAddress is required.")
            String roadAddress
    ) {
    }

    public record LocalSignup(
            @Schema(example = "김지홍")
            @NotBlank(message = "name is required.")
            @Size(max = 50, message = "name must be 50 characters or less.")
            String name,

            @Schema(example = "2002-03-11")
            @NotNull(message = "birthDate is required.")
            @PastOrPresent(message = "birthDate cannot be a future date.")
            LocalDate birthDate,

            @Schema(example = "MALE")
            @NotNull(message = "gender is required.")
            Gender gender,

            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = "^[0-9-]+$", message = "phoneNumber can contain only numbers and hyphens.")
            String phoneNumber,

            @Schema(example = "123456")
            @NotBlank(message = "password is required.")
            @Pattern(regexp = "^\\d{6}$", message = "password must be exactly 6 digits.")
            String password,

            @Schema(example = "고양시 덕양구 화랑로 28")
            @NotBlank(message = "roadAddress is required.")
            String roadAddress,

            @Schema(example = "송골매빌 B")
            String detailAddress,

            @Schema(example = "37.6013094206959")
            @NotNull(message = "latitude is required.")
            @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90.")
            @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90.")
            Double latitude,

            @Schema(example = "126.870894409123")
            @NotNull(message = "longitude is required.")
            @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180.")
            @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180.")
            Double longitude,

            @Schema(example = "1")
            @NotNull(message = "regionId is required.")
            Long regionId,

            @Schema(example = "가을")
            @NotBlank(message = "passwordAnswer is required.")
            String passwordAnswer
    ) {
    }

    public record KakaoLogin(
            @Schema(example = "kakao_authorization_code")
            @NotBlank(message = "authorizationCode is required.")
            String authorizationCode
    ) {
    }

    public record KakaoSignup(
            @Schema(example = "김지홍")
            @NotBlank(message = "name is required.")
            @Size(max = 50, message = "name must be 50 characters or less.")
            String name,

            @Schema(example = "2002-03-11")
            @NotNull(message = "birthDate is required.")
            @PastOrPresent(message = "birthDate cannot be a future date.")
            LocalDate birthDate,

            @Schema(example = "MALE")
            @NotNull(message = "gender is required.")
            Gender gender,

            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = "^[0-9-]+$", message = "phoneNumber can contain only numbers and hyphens.")
            String phoneNumber,

            @Schema(example = "고양시 덕양구 화랑로 28")
            @NotBlank(message = "roadAddress is required.")
            String roadAddress,

            @Schema(example = "송골매빌 B")
            String detailAddress,

            @Schema(example = "37.6013094206959")
            @NotNull(message = "latitude is required.")
            @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90.")
            @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90.")
            Double latitude,

            @Schema(example = "126.870894409123")
            @NotNull(message = "longitude is required.")
            @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180.")
            @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180.")
            Double longitude,

            @Schema(example = "1")
            @NotNull(message = "regionId is required.")
            Long regionId
    ) {
    }
}
