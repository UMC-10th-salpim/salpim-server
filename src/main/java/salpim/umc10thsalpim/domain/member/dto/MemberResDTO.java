package salpim.umc10thsalpim.domain.member.dto;

import salpim.umc10thsalpim.domain.member.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MemberResDTO {

    public record MyPageInfo(
            String name,
            LocalDate birthDate,
            Gender gender,
            String phoneNumber,
            String roadAddress,
            String detailAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            Long regionId,
            String sido,
            String sigungu,
            String generalGu,
            String administrativeArea
    ) {}

    public record PasswordVerificationResult(
            boolean isVerified
    ) {}
}
