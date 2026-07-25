package salpim.umc10thsalpim.domain.term.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AgreementErrorCode implements BaseErrorCode {

    TERM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "TERM404",
            "해당 약관을 찾을 수 없습니다."),
    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST,
            "TERM400_REQUIRED",
            "필수 약관에 동의해야 합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
