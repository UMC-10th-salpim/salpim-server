package salpim.umc10thsalpim.domain.welfare.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum WelfareErrorCode implements BaseErrorCode {

    BENEFIT_LIST_GET_FAILED(
            HttpStatus.NOT_FOUND,
            "BENEFIT404",
            "해당 혜택을 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

}
