package salpim.umc10thsalpim.domain.map.dto;


import lombok.Builder;
import lombok.Getter;

public class MapRequestDto {

    @Builder
    public record FacilityInfoRequest( //지도 혜택 조회용 응답 DTO
        String FacilityName,
        String address,
        String phoneNumber,
        String distanceNext
    ){}

}
