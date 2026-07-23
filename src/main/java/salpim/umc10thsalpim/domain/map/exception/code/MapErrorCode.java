package salpim.umc10thsalpim.domain.map.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MapErrorCode implements BaseErrorCode {

    // 400 Bad Request
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "MAP400_1", "유효하지 않은 위도/경도 좌표입니다."),
    INVALID_FACILITY_REQUEST(HttpStatus.BAD_REQUEST, "MAP400_2", "시설 정보 요청 값이 올바르지 않습니다."),
    NOT_MY_SERVICE_CENTER(HttpStatus.BAD_REQUEST, "MAP400_3", "요청하신 시설은 회원님의 관할 행정동이 아닙니다."),

    // 404 Not Found
    SERVICE_CENTER_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP404_2", "회원님의 관할 행정복지센터 정보를 찾을 수 없습니다."),
    FACILITY_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP404_3", "해당 시설 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
