package salpim.umc10thsalpim.domain.map.converter;

import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WelfareConverter {

    // 시설 상세 정보 응답 DTO 변환
    public MapResDTO.FacilityInfoResDTO toFacilityInfoResDTO(
            MapReqDTO.FacilityInfoRequest request,
            String calculatedDistance,
            boolean isMatched,
            MapResDTO.BenefitPageDTO benefits
    ) {
        return MapResDTO.FacilityInfoResDTO.builder()
                .name(request.facilityName())
                .address(request.address())
                .hour("09:00 - 18:00")
                .distanceText(calculatedDistance)
                .isMyCenter(isMatched)
                .benefits(benefits)
                .build();
    }

    // 중앙 혜택 리스트 변환 (BokjiroApiDTO -> DTO)
    public List<MapResDTO.BenefitDTO> toCentralBenefitDTO(BokjiroApiDTO.BenefitListRes response) {
        if (response == null || response.getBenefitList() == null) {
            return Collections.emptyList();
        }

        return response.getBenefitList().stream()
                .map(item -> MapResDTO.BenefitDTO.builder()
                        .servId(item.getServId())
                        .region("전국")
                        .serviceName(item.getServNm())
                        .build())
                .collect(Collectors.toList());
    }

    // 지자체 혜택 리스트 변환 (BokjiroApiDTO -> DTO)
    public List<MapResDTO.BenefitDTO> toLocalBenefitDTO(
            BokjiroApiDTO.BenefitListRes response,
            String sido,
            String sigungu
    ) {
        if (response == null || response.getBenefitList() == null) {
            return Collections.emptyList();
        }

        String fallbackRegion = (sido != null && sigungu != null) ? sido + " " + sigungu : "지자체";

        return response.getBenefitList().stream()
                .map(item -> {
                    String regionText = (item.getCtpvNm() != null && item.getSggNm() != null)
                            ? item.getCtpvNm() + " " + item.getSggNm()
                            : fallbackRegion;

                    return MapResDTO.BenefitDTO.builder()
                            .servId(item.getServId())
                            .region(regionText)
                            .serviceName(item.getServNm())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
