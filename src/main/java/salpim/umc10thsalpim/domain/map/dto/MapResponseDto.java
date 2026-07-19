package salpim.umc10thsalpim.domain.map.dto;

import lombok.Builder;

public class MapResponseDto {

    @Builder
    public record FacilityInfoResponseDto(
            String name,
            String address,
            String hour,
            String distanceText,
            boolean isMyCenter
            //빈 제작 메서드 반환
    ){}
}
