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

        if(isMatched){
            //중앙복지혜택리스트 조회
            //지자체복지혜택리스트 조회
        }

        if (!isMatched) {
            throw new IllegalArgumentException("요청하신 시설은 회원님의 관할 행정동이 아닙니다!");
        }

        return MapResponseDto.FacilityInfoResponseDto.builder()
                .name(request.FacilityName())
                .address(request.address())
                .hours("운영시간 정보 없음")//임시 하드코딩
                .phoneNumber("전화번호 정보 없음")
                .distanceNext("거리 정보 없음")
                .build();
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
