package salpim.umc10thsalpim.domain.auth.dto;

import lombok.Builder;

public class AuthResDTO {

    @Builder
    public record PhoneVerifyResult(
            Boolean verified
    ) {
    }

    @Builder
    public record GeocodeResult(
            String roadAddress,
            Double latitude,
            Double longitude
    ) {
    }
}
