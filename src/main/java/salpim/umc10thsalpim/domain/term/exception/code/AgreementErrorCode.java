package salpim.umc10thsalpim.domain.term.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AgreementErrorCode implements BaseErrorCode {

    TERM_NOT_FOUND(HttpStatus.NOT_FOUND,
            "TERM404_1",
            "해당 약관을 찾을 수 없습니다."),

    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST,
            "TERM400_1",
            "필수 약관에 동의해야 합니다."),
    DUPLICATE_TERMS_VERSION(HttpStatus.BAD_REQUEST,
            "TERM400_2",
            "동일한 약관 버전에 대한 동의 항목이 중복되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
