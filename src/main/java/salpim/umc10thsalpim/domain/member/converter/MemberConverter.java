package salpim.umc10thsalpim.domain.member.converter;

import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.region.entity.Region;

import java.math.BigDecimal;

public final class MemberConverter {

    private MemberConverter() {
    }

    public static Member toLocalMember(
            AuthReqDTO.LocalSignup request,
            String normalizedPhoneNumber,
            String encodedPassword,
            Region region
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
                .region(region)
                .passwordRecoveryAnswer(request.passwordAnswer().trim())
                .welfareCenter(region.getName())
                .build();
    }

    public static Member toKakaoMember(
            AuthReqDTO.KakaoSignup request,
            String normalizedPhoneNumber,
            String kakaoId,
            Region region
    ) {
        return Member.builder()
                .loginType(SocialProvider.KAKAO)
                .phoneNumber(normalizedPhoneNumber)
                .kakaoId(kakaoId)
                .name(request.name().trim())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .roadAddress(request.roadAddress().trim())
                .detailAddress(normalizeNullableText(request.detailAddress()))
                .latitude(BigDecimal.valueOf(request.latitude()))
                .longitude(BigDecimal.valueOf(request.longitude()))
                .region(region)
                .build();
    }

    public static MemberResDTO.MyPageInfo toMyPageInfo(
            Member member,
            String sido,
            String sigungu
    ) {
        return new MemberResDTO.MyPageInfo(
                member.getName(),
                sido,
                sigungu
        );
    }

    public static String normalizeNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
