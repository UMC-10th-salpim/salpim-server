package salpim.umc10thsalpim.domain.map.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum MapSuccessCode implements BaseSuccessCode {

    FACILITY_INFO_SUCCESS(HttpStatus.OK, "MAP2000", "시설 상세 정보 및 복지 혜택을 성공적으로 조회했습니다."),
    NEARBY_FACILITY_SEARCH_SUCCESS(HttpStatus.OK, "MAP2001", "주변 시설 목록을 성공적으로 조회했습니다."),
    MAP_SEARCH_SUCCESS(HttpStatus.OK, "MAP2002", "지도 검색 결과를 성공적으로 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
