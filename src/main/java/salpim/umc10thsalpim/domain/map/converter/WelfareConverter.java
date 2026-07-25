package salpim.umc10thsalpim.domain.map.converter;

import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.map.dto.ExternalWelfareResponse;
import salpim.umc10thsalpim.domain.map.dto.MapRequestDto;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WelfareConverter {

    // 시설 상세 정보 응답 DTO 변환
    public MapResponseDto.FacilityInfoResponseDto toFacilityInfoResponseDto(
            MapRequestDto.FacilityInfoRequest request,
            String calculatedDistance,
            boolean isMatched,
            MapResponseDto.BenefitPageDto benefits
    ) {
        return MapResponseDto.FacilityInfoResponseDto.builder()
                .name(request.facilityName())
                .address(request.address())
                .hour("09:00 - 18:00")
                .distanceText(calculatedDistance)
                .isMyCenter(isMatched)
                .benefits(benefits)
                .build();
    }

    //중앙 혜택리스트 변환 (XML 요소 -> DTO 요소)
    public List<MapResponseDto.BenefitDto> toCentralBenefitDto(ExternalWelfareResponse response) {
        if(response == null || response.getServList() == null){
            return Collections.emptyList();
        }

        return response.getServList().stream()
                .map(item -> MapResponseDto.BenefitDto.builder()
                        .servId(item.getServId())
                        .region("전국")
                        .serviceName(item.getServNm())
                        .build())
                .collect(Collectors.toList());
    }

    //지자체 혜택리스트 변환 (XML 요소 -> DTO 요소)
    public List<MapResponseDto.BenefitDto> toLocalBenefitDto(ExternalWelfareResponse response) {
        if(response == null || response.getServList() == null) return Collections.emptyList();

        return response.getServList().stream()
                .map(item -> MapResponseDto.BenefitDto.builder()
                        .servId(item.getServId())
                        .region(item.getCtpvNm() + " " + item.getSggNm())
                        .serviceName(item.getServNm())
                        .build())
                .collect(Collectors.toList());
    }
}
