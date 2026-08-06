package salpim.umc10thsalpim.domain.map.converter;

import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public List<MapResDTO.BenefitDTO> toBenefitDTOList(
            List<WelfareBenefit> benefits,
            Map<Long, String> regionNameMap
    ) {
        if (benefits == null || benefits.isEmpty()) {
            return Collections.emptyList();
        }

        return benefits.stream()
                .map(benefit -> toBenefitDTO(benefit, regionNameMap))
                .collect(Collectors.toList());
    }

    //welfareBenefit 엔티티를 BenefitDTO로
    private MapResDTO.BenefitDTO toBenefitDTO(
            WelfareBenefit benefit,
            Map<Long, String> regionNameMap
    ) {
        // 지역(region) 문자열 처리: 중앙 혜택이면 '전국', 아니면 '지자체' (필요시 지자체 명으로 고도화 가능)
        String regionText = "전국";

        if("LOCAL".equals(benefit.getSource()) && benefit.getRegionId() != null){
            regionText = regionNameMap.getOrDefault(benefit.getRegionId(), "지자체");
        }

        return MapResDTO.BenefitDTO.builder()
                .benefitId(benefit.getId()) //페이징에 사용할 DB PK
                .servId(benefit.getExternalId()) // 자세히 보기에 사용할 서비스 ID
                .region(regionText)
                .serviceName(benefit.getTitle())
                .build();
    }
}
