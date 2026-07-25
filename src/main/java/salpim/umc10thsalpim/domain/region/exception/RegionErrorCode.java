package salpim.umc10thsalpim.domain.region.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum RegionErrorCode implements BaseErrorCode {

    REGION_NOT_FOUND(HttpStatus.NOT_FOUND,
            "REGION404",
            "해당 지역을 찾을 수 없습니다."),
    INVALID_REGION_REQUEST(HttpStatus.BAD_REQUEST,
            "REGION400_REQUEST",
            "잘못된 지역 요청입니다."),
    REGION_NOT_LEAF(HttpStatus.BAD_REQUEST,
            "REGION400_NOT_LEAF",
            "회원가입 지역은 읍·면·동 단위여야 합니다."),
    REGION_CONFLICT_RETRY_FAILED(HttpStatus.CONFLICT,
            "REGION409_CONFLICT",
            "지역 저장 중 중복 충돌이 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
