package salpim.umc10thsalpim.domain.auth.converter;

import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.GeocodingClientResDTO;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.member.entity.Member;

import java.time.LocalDateTime;

public final class AuthConverter {

    private AuthConverter() {
    }

    public static PhoneVerification toPhoneVerification(
            Member member,
            String phoneNumber,
            PhoneVerificationPurpose purpose,
            String code,
            LocalDateTime expiredAt
    ) {
        return PhoneVerification.builder()
                .member(member)
                .phoneNumber(phoneNumber)
                .purpose(purpose)
                .code(code)
                .expiredAt(expiredAt)
                .verified(false)
                .build();
    }

    public static AuthResDTO.PhoneVerifyResult toPhoneVerifyResult(Boolean verified) {
        return AuthResDTO.PhoneVerifyResult.builder()
                .verified(verified)
                .build();
    }

    public static AuthResDTO.GeocodeResult toGeocodeResult(
            String roadAddress,
            GeocodingClientResDTO.Coordinate coordinate
    ) {
        return AuthResDTO.GeocodeResult.builder()
                .roadAddress(roadAddress)
                .latitude(coordinate.latitude())
                .longitude(coordinate.longitude())
                .build();
    }
}
