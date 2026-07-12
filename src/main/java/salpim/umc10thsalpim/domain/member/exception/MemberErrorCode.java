package salpim.umc10thsalpim.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT,
            "MEMBER409_PHONE",
            "이미 가입된 전화번호입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
