package salpim.umc10thsalpim.domain.map.dto;


import lombok.Builder;

public class MapResponseDto {

    @Builder
    public record FacilityInfoResponse( //지도 혜택 조회용 응답 DTO
        String name,
        String address,
        String hours,
        String phoneNumber,
        String distanceNext
    ){}

}
