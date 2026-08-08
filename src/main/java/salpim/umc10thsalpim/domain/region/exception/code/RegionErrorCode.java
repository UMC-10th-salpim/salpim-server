package salpim.umc10thsalpim.domain.region.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum RegionErrorCode implements BaseErrorCode {

    INVALID_REGION_REQUEST(HttpStatus.BAD_REQUEST,
            "REGION400_REQUEST",
            "잘못된 지역 요청입니다."),
    REGION_SEARCH_LEVEL_INVALID(
            HttpStatus.BAD_REQUEST,
            "REGION400_LEVEL",
            "지역은 시도 1개와 시군구 1개를 입력해야 합니다."),
    REGION_HIERARCHY_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "REGION400_HIERARCHY",
            "선택한 지역의 계층 관계가 올바르지 않습니다."),
    REGION_NOT_LEAF(HttpStatus.BAD_REQUEST,
            "REGION400_NOT_LEAF",
            "회원가입 지역은 읍면동 단위여야 합니다."),
    REGION_CONFLICT_RETRY_FAILED(HttpStatus.CONFLICT,
            "REGION409_CONFLICT",
            "지역 저장 중 중복 충돌이 발생했습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND,
            "REGION404",
            "해당 지역을 찾을 수 없습니다."),
    REGION_HIERARCHY_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,
            "REGION500_1",
            "지역의 계층 정보가 올바르지 않습니다."),
    REGION_LEVEL_INVALID(HttpStatus.BAD_REQUEST,
            "REGION400_1",
            "동 단위 지역만 설정할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
