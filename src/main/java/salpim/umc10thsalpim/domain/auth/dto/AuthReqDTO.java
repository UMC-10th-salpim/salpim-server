package salpim.umc10thsalpim.domain.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import salpim.umc10thsalpim.domain.member.enums.Gender;

import java.time.LocalDate;
import java.util.List;

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

    public record Geocode(
            @NotBlank(message = "도로명 주소는 필수입니다.")
            String roadAddress
    ) {
    }

    public record LocalSignup(
            @NotBlank(message = "이름은 필수입니다.")
            @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
            String name,

            @NotNull(message = "생년월일은 필수입니다.")
            @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
            LocalDate birthDate,

            @NotNull(message = "성별은 필수입니다.")
            Gender gender,

            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자 또는 하이픈만 입력할 수 있습니다.")
            String phoneNumber,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.")
            String password,

            @NotBlank(message = "도로명 주소는 필수입니다.")
            String roadAddress,

            String detailAddress,

            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            Double latitude,

            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            Double longitude,

            @NotNull(message = "지역 ID는 필수입니다.")
            Long regionId,

            @NotEmpty(message = "동의한 약관 ID는 1개 이상이어야 합니다.")
            List<Long> agreedTermIds,

            @NotBlank(message = "비밀번호 복구 답변은 필수입니다.")
            String passwordAnswer
    ) {
    }
}
