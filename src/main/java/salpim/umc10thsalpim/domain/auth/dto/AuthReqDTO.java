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
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber,

            @Schema(example = "qwer1234")
            @NotBlank(message = "비밀번호는 필수입니다.")
            String password
    ) {
    }

    public record TokenReissue(
            @Schema(example = "refresh_token")
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {
    }

    public record PhoneSend(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber
    ) {
    }

    public record PhoneVerify(
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
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
            @Schema(example = "김지홍")
            @NotBlank(message = "이름은 필수입니다.")
            @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
            String name,

            @Schema(example = "2002-03-11")
            @NotNull(message = "생년월일은 필수입니다.")
            @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
            LocalDate birthDate,

            @Schema(example = "MALE")
            @NotNull(message = "성별은 필수입니다.")
            Gender gender,

            @Schema(example = "MEDIUM")
            @NotNull(message = "글자 크기는 필수입니다.")
            WordSize wordSize,

            @Schema(example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber,

            @Schema(example = "123456")
            @NotBlank(message = "비밀번호는 필수입니다.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "비밀번호는 6자리 숫자여야 합니다."
            )
            String password,

            @Schema(example = "고양시 덕양구 화랑로 28")
            @NotBlank(message = "도로명 주소는 필수입니다.")
            String roadAddress,

            @Schema(example = "송골매빌 B")
            String detailAddress,

            @Schema(example = "37.6013094206959")
            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            Double latitude,

            @Schema(example = "126.870894409123")
            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            Double longitude,

            @Schema(example = "1")
            @NotNull(message = "지역 ID는 필수입니다.")
            Long regionId,

            @Schema(example = "가을")
            @NotBlank(message = "비밀번호 복구 답변은 필수입니다.")
            String passwordAnswer
    ) {
    }

    public record SignupTermsAgreement(
            @Schema(example = "01012345678")
            @NotBlank(message = "전화번호는 필수입니다.")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber,

            @NotEmpty(message = "동의할 약관 목록은 필수입니다.")
            @Valid
            List<TermReqDTO.@NotNull(message = "약관 동의 항목은 null일 수 없습니다.") AgreementItem> agreements
    ) {
    }

    public record KakaoLogin(
            @Schema(example = "kakao_authorization_code")
            @NotBlank(message = "카카오 인가 코드는 필수입니다.")
            String authorizationCode
    ) {
    }

    public record KakaoSignup(
            @Schema(example = "김지홍")
            @NotBlank(message = "이름은 필수입니다.")
            @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
            String name,

            @Schema(example = "2002-03-11")
            @NotNull(message = "생년월일은 필수입니다.")
            @PastOrPresent(message = "생년월일은 미래 날짜일 수 없습니다.")
            LocalDate birthDate,

            @Schema(example = "MALE")
            @NotNull(message = "성별은 필수입니다.")
            Gender gender,

            @Schema(example = "MEDIUM")
            @NotNull(message = "글자 크기는 필수입니다.")
            WordSize wordSize,

            @Schema(example = "01012345678")
            @Pattern(regexp = KOREAN_MOBILE_PHONE_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다.")
            String phoneNumber,

            @Schema(example = "고양시 덕양구 화랑로 28")
            @NotBlank(message = "도로명 주소는 필수입니다.")
            String roadAddress,

            @Schema(example = "송골매빌 B")
            String detailAddress,

            @Schema(example = "37.6013094206959")
            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            Double latitude,

            @Schema(example = "126.870894409123")
            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            Double longitude,

            @Schema(example = "1")
            @NotNull(message = "지역 ID는 필수입니다.")
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