package salpim.umc10thsalpim.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_VERIFICATION",
            "인증번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_VERIFICATION_EXPIRED",
            "인증번호가 만료되었습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST,
            "AUTH400_PHONE_NOT_VERIFIED",
            "휴대폰 인증이 완료되지 않았습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
