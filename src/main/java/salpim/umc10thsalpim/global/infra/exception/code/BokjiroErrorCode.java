package salpim.umc10thsalpim.global.infra.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum BokjiroErrorCode implements BaseErrorCode {

    BOKJIRO_API_ERROR(HttpStatus.BAD_GATEWAY,
            "BOKJIRO502",
            "복지로 API 호출에 실패했습니다."),
    BOKJIRO_PARSE_ERROR(HttpStatus.BAD_GATEWAY,
            "BOKJIRO502_1",
            "복지로 API 응답을 처리할 수 없습니다."),
    BOKJIRO_TIME_OUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "BOKJIRO504",
            "복지로 API 응답 시간이 초과되었습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;
}
