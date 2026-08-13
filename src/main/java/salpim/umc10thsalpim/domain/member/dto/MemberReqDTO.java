package salpim.umc10thsalpim.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.PasswordVerificationMethod;
import salpim.umc10thsalpim.domain.member.enums.WordSize;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MemberReqDTO {

    public record UpdateProfile(
            @Schema(description = "회원 이름", example = "김살핌")
            @NotBlank
            String name,

            @Schema(description = "생년월일, 과거날짜만 사용가능합니다.", example = "1950-11-11")
            @NotNull
            @Past
            LocalDate birthDate,

            @Schema(description = "성별", example = "FEMALE")
            @NotNull
            Gender gender,

            @Schema(description = "도로명 주소", example = "고양시 덕양구 화랑로 28")
            @NotBlank
            String roadAddress,

            @Schema(description = "상세 주소, 생략하거나 공백으로 보내면 null로 저장됩니다.", example = "송골매빌 B")
            String detailAddress,

            @Schema(description = "위도, 소수점 7자리까지 허용됩니다.", example = "37.6013094")
            @NotNull
            @DecimalMin(value = "-90.0")
            @DecimalMax(value = "90.0")
            @Digits(integer = 3, fraction = 7)
            BigDecimal latitude,

            @Schema(description = "경도, 소수점 7자리까지 허용됩니다.", example = "126.8708944")
            @NotNull
            @DecimalMin(value = "-180.0")
            @DecimalMax(value = "180.0")
            @Digits(integer = 3, fraction = 7)
            BigDecimal longitude,

            @Schema(description = "거주 지역 ID, 읍/면/동 단위만 설정할 수 있습니다.", example = "1")
            @NotNull
            @Positive
            Long regionId,

            @Schema(description = "변경할 전화번호", example = "01012345678")
            @Pattern(regexp = "^[0-9-]+$")
            String phoneNumber,

            @Schema(description = "전화번호 변경 인증번호 검증 API에서 받은 토큰. 발급 후 10분간 유효하며 한 번만 사용할 수 있습니다.")
            String phoneVerificationToken
    ){}

    public record UpdateWordSize(
            @Schema(description = "설정할 글자 크기", example = "LARGE")
            @NotNull
            WordSize wordSize
    ){}

    public record VerifyCurrentPassword(
            @Schema(description = "현재 비밀번호, 6자리 숫자입니다.", example = "123456")
            @NotBlank
            @Pattern(regexp = "^\\d{6}$")
            String currentPassword
    ) {}

    public record VerifyRecoveryAnswer(
            @NotBlank
            @Size(max = 255)
            @Schema(description = "회원가입 시 등록한 비밀번호 찾기 답변")
            String recoveryAnswer
    ) {}

    public record ChangePassword(
            @Schema(description = "본인 확인 방식, CURRENT_PASSWORD 혹은 RECOVERY_ANSWER")
            @NotNull
            PasswordVerificationMethod verificationMethod,

            @Schema(description = "현재 비밀번호. verificationMethod가 CURRENT_PASSWORD인 경우에만 보냅니다.", example = "123456")
            @Pattern(regexp = "^\\d{6}$")
            String currentPassword,

            @Schema(description = "비밀번호 찾기 답변. verificationMethod가 RECOVERY_ANSWER인 경우에만 보냅니다.")
            @Size(max = 255)
            String recoveryAnswer,

            @Schema(description = "새 비밀번호", example = "654321")
            @NotBlank
            @Pattern(regexp = "^\\d{6}$")
            String newPassword
    ) {}
}
