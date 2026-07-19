package salpim.umc10thsalpim.domain.map.dto;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

public class MapRequestDto {

    @Builder
    public record FacilityInfoRequest( //지도 혜택 조회용 응답 DTO
        String FacilityName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
    ){}
}
