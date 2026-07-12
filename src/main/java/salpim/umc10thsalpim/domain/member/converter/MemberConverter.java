package salpim.umc10thsalpim.domain.member.converter;

import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

import java.math.BigDecimal;

public final class MemberConverter {

    private MemberConverter() {
    }

    public static Member toLocalMember(
            AuthReqDTO.LocalSignup request,
            String normalizedPhoneNumber,
            String encodedPassword
    ) {
        return Member.builder()
                .loginType(SocialProvider.LOCAL)
                .phoneNumber(normalizedPhoneNumber)
                .password(encodedPassword)
                .name(request.name().trim())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .roadAddress(request.roadAddress().trim())
                .detailAddress(normalizeNullableText(request.detailAddress()))
                .latitude(BigDecimal.valueOf(request.latitude()))
                .longitude(BigDecimal.valueOf(request.longitude()))
                .regionId(request.regionId())
                .passwordRecoveryAnswer(request.passwordAnswer().trim())
                .build();
    }

    private static String normalizeNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
