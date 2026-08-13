package salpim.umc10thsalpim.domain.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.map.converter.WelfareConverter;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.domain.map.exception.MapException;
import salpim.umc10thsalpim.domain.map.exception.code.MapErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.util.GeoUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final MemberRepository memberRepository;
    private final RegionQueryService regionQueryService;

    private final WelfareBenefitRepository welfareBenefitRepository;
    private final WelfareConverter welfareConverter;

    public MapResDTO.FacilityInfoResDTO getFacilityInfo(
            Long memberId,
            MapReqDTO.FacilityInfoRequest request,
            String cursor,
            int size
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 좌표 유효성 검증 (GeoUtils 사용)
        GeoUtils.validateCoordinates(request.latitude(), request.longitude());
        GeoUtils.validateCoordinates(member.getLatitude(), member.getLongitude());

        //DB에 저장된 회원의 welfare_center 유효성 검증
        if(member.getWelfareCenter() == null || member.getWelfareCenter().isBlank()){
            throw new MapException(MapErrorCode.SERVICE_CENTER_NOT_FOUND); //404_2
        }
        //카카오맵 요청 facilityName 유효성 검증
        if(request.facilityName() == null || request.facilityName().isBlank()){
            throw new MapException(MapErrorCode.INVALID_FACILITY_REQUEST); //400_2
        }

        // 관할 행정복지센터 일치 여부 확인 메서드 호출(카카오맵 VS DB의 사용자 관할행정동)
        validateMyServiceCenter(member.getWelfareCenter(), request.facilityName());
        //예외가 터지지 않으면 true
        boolean isMatched = true;

        // 거리 계산 (GeoUtils 사용)
        String calculatedDistance = GeoUtils.calculateDistance(
                member.getLatitude(), member.getLongitude(),
                request.latitude(), request.longitude()
        );


        Map<Long, String> regionNameMap = regionQueryService.getAncestorRegionNameMap(member.getRegionId());
        List<Long> upperRegionIds = new ArrayList<>(regionNameMap.keySet());

        Long cursorId = parseCursor(cursor);
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        // DB 단일 쿼리로 중앙 + 지자체 혜택 중 VISIT만 페이징
        List<WelfareBenefit> queriedBenefits = welfareBenefitRepository.findWelfareBenefitsByRegionAndCursorAndAppType(
                upperRegionIds,
                ApplicationType.VISIT,
                cursorId,
                pageRequest
        );

        boolean hasNext = queriedBenefits.size() > size;
        if(hasNext){
            queriedBenefits.remove(size);
        }

        //엔티티를 DTO리스트로 변환
        List<MapResDTO.BenefitDTO> benefitDTOList = welfareConverter.toBenefitDTOList(queriedBenefits, regionNameMap);

        String nextCursor = (hasNext && !queriedBenefits.isEmpty())
                ? String.valueOf(queriedBenefits.get(queriedBenefits.size() - 1).getId())
                : null;

        MapResDTO.BenefitPageDTO benefitPageDTO = MapResDTO.BenefitPageDTO.builder()
                .data(benefitDTOList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(benefitDTOList.size())
                .build();

        return welfareConverter.toFacilityInfoResDTO(request, calculatedDistance, isMatched, benefitPageDTO);
    }

    // 지도에서 선택한 마커가 사용자의 관할구가 맞는지 판별
    private void validateMyServiceCenter(String dbDongName, String kakaoFacilityName) {

        //공백, 쉼표, 마침표, 기호 등 문자 제거
        String myDong = dbDongName.replaceAll("[^가-힣0-9]",""); // DB에서 추출한 동 이름 (예: "학익1동")
        String normalizedKakao = kakaoFacilityName.replaceAll("[^가-힣0-9]",""); // 카카오맵 facilityName

        boolean isWelfareCenter = normalizedKakao.contains("주민센터") || normalizedKakao.contains("행정복지센터");

        //넘어온 시설이 주민센터, 행정복지센터인지 확인
        if(!isWelfareCenter){
            throw new MapException(MapErrorCode.NOT_WELFARE_CENTER); //400_4
        }

        //내 관활동 이름이 포함되어 있는지 확인
        boolean containsDong = normalizedKakao.contains(myDong);

        if(!containsDong){
            throw new MapException(MapErrorCode.NOT_MY_SERVICE_CENTER); //400_3
        }
    }

    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new MapException(MapErrorCode.INVALID_CURSOR);
        }
    }
}
