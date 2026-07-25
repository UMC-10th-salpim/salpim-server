package salpim.umc10thsalpim.domain.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.map.converter.WelfareConverter;
import salpim.umc10thsalpim.domain.map.dto.ExternalWelfareResponse;
import salpim.umc10thsalpim.domain.map.dto.MapRequestDto;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;
import salpim.umc10thsalpim.domain.map.exception.MapException;
import salpim.umc10thsalpim.domain.map.exception.code.MapErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.util.GeoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final MemberRepository memberRepository;
    private final RegionQueryService regionQueryService;
    private final WelfareApiClient welfareApiClient;
    private final WelfareConverter welfareConverter;

    public MapResponseDto.FacilityInfoResponseDto getFacilityInfo(
            Long memberId,
            MapRequestDto.FacilityInfoRequest request
    ) {
        String cursor = request.cursor();
        int size = (request.size() != null && request.size() > 0) ? request.size() : 10;
        return getFacilityInfo(memberId, request, cursor, size);
    }

    public MapResponseDto.FacilityInfoResponseDto getFacilityInfo(
            Long memberId,
            MapRequestDto.FacilityInfoRequest request,
            String cursor,
            int size
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 좌표 유효성 검증 (GeoUtils 사용)
        GeoUtils.validateCoordinates(request.latitude(), request.longitude());
        GeoUtils.validateCoordinates(member.getLatitude(), member.getLongitude());

        // 관할 행정복지센터 일치 여부 확인 (카카오맵 VS DB의 사용자 관할행정동)
        boolean isMatched = isMyServiceCenter(member.getWelfareCenter(), request.facilityName());
        if (!isMatched) {
            log.warn("⚠️ 관할 행정동 불일치 - Member serviceCenter: {}, Request facilityName: {}", member.getWelfareCenter(), request.facilityName());
            throw new MapException(MapErrorCode.NOT_MY_SERVICE_CENTER);
        }

        // 거리 계산 (GeoUtils 사용)
        String calculatedDistance = GeoUtils.calculateDistance(
                member.getLatitude(), member.getLongitude(),
                request.latitude(), request.longitude()
        );

        List<MapResponseDto.BenefitDto> totalBenefits = new ArrayList<>();

        // 중앙 혜택 리스트 추가
        ExternalWelfareResponse centralResponse = welfareApiClient.fetchRawCentralBenefits();
        totalBenefits.addAll(welfareConverter.toCentralBenefitDto(centralResponse));

        // 지자체 혜택 리스트 추가 (RegionQueryService 사용)
        if (member.getRegionId() != null) {
            String[] location = regionQueryService.getSidoAndSigungu(member.getRegionId());
            String sido = location[0];
            String sigungu = location[1];

            if (sido != null && sigungu != null) {
                ExternalWelfareResponse localResponse = welfareApiClient.fetchRawLocalBenefits(sido, sigungu);
                totalBenefits.addAll(welfareConverter.toLocalBenefitDto(localResponse));
            }
        }

        // 메모리 기반 커서 페이징 처리
        MapResponseDto.BenefitPageDto benefitPageDto = paginateBenefits(totalBenefits, cursor, size);

        return welfareConverter.toFacilityInfoResponseDto(request, calculatedDistance, isMatched, benefitPageDto);
    }

    /**
     * 메모리 커서 페이징 처리 메서드
     */
    public MapResponseDto.BenefitPageDto paginateBenefits(
            List<MapResponseDto.BenefitDto> allBenefits,
            String cursor,
            int size
    ) {
        // 1. 전체 데이터 집계 및 totalCount 저장
        int totalCount = (allBenefits != null) ? allBenefits.size() : 0;
        int pageSizeLimit = (size <= 0) ? 10 : size;

        if (allBenefits == null || allBenefits.isEmpty()) {
            return MapResponseDto.BenefitPageDto.builder()
                    .data(Collections.emptyList())
                    .hasNext(false)
                    .nextCursor(null)
                    .pageSize(0)
                    .totalCount(0)
                    .build();
        }

        // 2. 커서 시작점(startIndex) 탐색
        int startIndex = 0;
        if (cursor != null && !cursor.isBlank()) {
            int foundIndex = -1;
            for (int i = 0; i < totalCount; i++) {
                if (cursor.equals(allBenefits.get(i).servId())) {
                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex != -1) {
                startIndex = foundIndex + 1;
            } else {
                // (방어 로직) 전달받은 cursor와 일치하는 ID가 리스트 내에 존재하지 않을 경우 빈 페이지 안전하게 반환
                return MapResponseDto.BenefitPageDto.builder()
                        .data(Collections.emptyList())
                        .hasNext(false)
                        .nextCursor(null)
                        .pageSize(0)
                        .totalCount(totalCount)
                        .build();
            }
        }

        // startIndex가 totalCount 이상이면 남은 데이터가 없음
        if (startIndex >= totalCount) {
            return MapResponseDto.BenefitPageDto.builder()
                    .data(Collections.emptyList())
                    .hasNext(false)
                    .nextCursor(null)
                    .pageSize(0)
                    .totalCount(totalCount)
                    .build();
        }

        // 3. 메모리 슬라이싱 (size + 1 기법)
        int endIndex = Math.min(startIndex + pageSizeLimit + 1, totalCount);
        List<MapResponseDto.BenefitDto> slicedList = new ArrayList<>(allBenefits.subList(startIndex, endIndex));

        // 4. 페이징 메타데이터 연산
        boolean hasNext = false;
        if (slicedList.size() > pageSizeLimit) {
            hasNext = true;
            slicedList.remove(slicedList.size() - 1); // 확인용으로 가져온 마지막 1개 요소 제거
        }

        String nextCursor = null;
        if (hasNext && !slicedList.isEmpty()) {
            nextCursor = slicedList.get(slicedList.size() - 1).servId();
        }

        int pageSize = slicedList.size();

        // 5. 최종 조립
        return MapResponseDto.BenefitPageDto.builder()
                .data(slicedList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(pageSize)
                .totalCount(totalCount)
                .build();
    }

    // 지도에서 선택한 마커가 사용자의 관할구가 맞는지 판별
    private boolean isMyServiceCenter(String dbDongName, String kakaoFacilityName) {
        if (dbDongName == null || kakaoFacilityName == null || dbDongName.isBlank()) {
            return false;
        }

        String myDong = dbDongName.trim(); // DB에서 추출한 동 이름 (예: "학익1동")
        String normalizedKakao = kakaoFacilityName.replaceAll("\\s+", ""); // 카카오맵 이름 공백 제거

        return normalizedKakao.contains(myDong) &&
                (normalizedKakao.contains("주민센터") || normalizedKakao.contains("행정복지센터"));
    }
}
