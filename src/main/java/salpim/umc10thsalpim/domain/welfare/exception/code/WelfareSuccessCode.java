package salpim.umc10thsalpim.domain.welfare.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum WelfareSuccessCode implements BaseSuccessCode {

    BENEFIT_LIST_GET_SUCCESS(
            HttpStatus.OK,
            "BENEFIT200",
            "성공적으로 혜택 목록이 조회되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
