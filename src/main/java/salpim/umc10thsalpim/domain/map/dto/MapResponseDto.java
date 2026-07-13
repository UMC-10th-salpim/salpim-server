package salpim.umc10thsalpim.domain.map.dto;

import lombok.Builder;

public class MapResponseDto {

    @Builder
    public record FacilityInfoResponseDto(
            String name,
            String address,
            String hours,
            String phoneNumber,
            String distanceNext
    ){}
}
