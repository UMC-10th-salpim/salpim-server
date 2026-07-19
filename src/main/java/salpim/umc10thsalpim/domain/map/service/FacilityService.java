package salpim.umc10thsalpim.domain.map.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.map.dto.MapRequestDto;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final MemberRepository memberRepository;

    public MapResponseDto.FacilityInfoResponseDto getFacilityInfo(
            Long memberId,
            MapRequestDto.FacilityInfoRequest request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

        // 관할 행정복지센터가 일치 여부 확인 (카카오맵 VS DB의 사용자 관할행정동)
        boolean isMatched = isMyServiceCenter(member.getServiceCenter(), request.FacilityName());

        //거리 계산
        String calculatedDistance = calculateDistance(
                member.getLatitude(), member.getLongitude(),
                request.latitude(), request.longitude()
        );

        if(isMatched){
            //중앙복지혜택리스트 조회 --> 지역은 직접 넣어야 함. / 빈 제작 메서드
            //지자체복지혜택리스트 조회 --> 지역은 직접 넣어야 함. / 빈 제작 메서드
        }

        if (!isMatched) {
            throw new IllegalArgumentException("요청하신 시설은 회원님의 관할 행정동이 아닙니다!");
        }

        return MapResponseDto.FacilityInfoResponseDto.builder()
                .name(request.FacilityName())
                .address(request.address())
                .hour("09:00 - 18:00")
                .distanceText(calculatedDistance)
                .build();
    }

    private String calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return "거리 정보 없음";
        }

        double earthRadius = 6371.0; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c; // 결과는 km 단위

        // 1km 미만이면 m 단위로, 1km 이상이면 소수점 첫째 자리 km 단위로 반환
        if (distance < 1.0) {
            return (int) (distance * 1000) + "m";
        }
        return String.format("%.1fkm", distance);
    }

    private boolean isMyServiceCenter(String dbCenterName, String kakaoFacilityName){
        if(dbCenterName == null || kakaoFacilityName == null || dbCenterName.isBlank()){
            return false;
        }

        String[] parts = dbCenterName.split(" "); //관할구 요소 별 추출 "인천광역시 / 미추홀구 / 학익1동"
        String myDong = parts[parts.length - 1]; //"학익1동 선택"
        String normalizedKakao = kakaoFacilityName.replaceAll("\\s+", ""); //카카오맵에서 받은 이름에서 공백 제거

        //내 행정동을 무조건 포함하고 "주민센터"나 "행정복지센터"가 있다면 True
        return normalizedKakao.contains(myDong) &&
                (normalizedKakao.contains("주민센터") || normalizedKakao.contains("행정복지센터"));
    }
}
