package salpim.umc10thsalpim.domain.map.converter;

import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;

public class MapConverter {

    public static MapResponseDto.FacilityInfoResponse toFacilityInfoResponse(
            String name,
            String address,
            String hours,
            String phoneNumber,
            String distanceNext)
    {
        return MapResponseDto.FacilityInfoResponse.builder()
                .name(name)
                .address(address)
                .hours("09:00 ~ 18:00 (주말 휴무)") //영업시간 하드 코딩
                .phoneNumber(phoneNumber != null ? phoneNumber : "전화번호 정보 없음" ) //전화번호 하드 코딩
                .distanceNext(distanceNext != null ? distanceNext + "거리입니다." : "거리 정보 없음") //거리 정보 하드코딩
                .build();
    }
}
