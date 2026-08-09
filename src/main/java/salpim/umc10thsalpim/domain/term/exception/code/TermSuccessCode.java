package salpim.umc10thsalpim.domain.term.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum TermSuccessCode implements BaseSuccessCode {

    TERM_VIEW(HttpStatus.OK,
            "TERM200_1",
            "약관 정보를 성공적으로 조회했습니다."),
    TERM_AGREEMENT_SUBMITTED(HttpStatus.CREATED,
            "TERM201_1",
            "약관 동의를 성공적으로 제출했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
