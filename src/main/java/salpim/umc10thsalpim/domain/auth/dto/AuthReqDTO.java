package salpim.umc10thsalpim.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.WordSize;
import salpim.umc10thsalpim.domain.term.dto.TermReqDTO;

import java.time.LocalDate;
import java.util.List;

public class AuthReqDTO {

    private static final String KOREAN_MOBILE_PHONE_PATTERN = "^01[016789]-?\\d{3,4}-?\\d{4}$";
    public record LocalLogin(
            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
            String phoneNumber,

            @Schema(example = "123456")
            @NotBlank(message = "password is required.")
            String password
    ) {
    }

    public record TokenReissue(
            @Schema(example = "refresh_token")
            @NotBlank(message = "refreshToken is required.")
            String refreshToken
    ) {
    }

    public record PhoneSend(
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
            String phoneNumber
    ) {
    }

    public record PhoneVerify(
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
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

            @Schema(example = "MEDIUM")
            @NotNull(message = "wordSize is required.")
            WordSize wordSize,

            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
            String phoneNumber,

            @Schema(example = "123456")
            @NotBlank(message = "password is required.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "password must be exactly 6 digits."
            )
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

    public record SignupTermsAgreement(
            @Schema(example = "01012345678")
            @NotBlank(message = "phoneNumber is required.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
            String phoneNumber,

            @NotEmpty(message = "동의할 약관 목록은 필수입니다.")
            @Valid
            List<TermReqDTO.@NotNull(message = "약관 동의 항목은 null일 수 없습니다.") AgreementItem> agreements
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

            @Schema(example = "MEDIUM")
            @NotNull(message = "wordSize is required.")
            WordSize wordSize,

            @Schema(example = "01012345678")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "phoneNumber must be a valid Korean mobile number.")
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

    public record PasswordResetVerify(
            @Schema(example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(
                    regexp = "^[0-9-]+$",
                    message = "전화번호는 숫자와 -만으로 이루어져야 합니다."
            )
            String phoneNumber,

            @Schema(example = "짜장면")
            @NotBlank(message = "복구 질문 답변은 필수입니다.")
            @Size(max = 255, message = "복구 질문 답변은 255자 이하여야 합니다.")
            String recoveryAnswer
    ){
    }

    public record PasswordReset(
            @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
            String passwordResetToken,

            @NotBlank(message = "새 비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "비밀번호는 6자리 숫자여야 합니다.")
            String newPassword
    ){
    }
}
