package salpim.umc10thsalpim.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    LOGIN_SUCCESS(HttpStatus.OK,
            "AUTH200_LOGIN",
            "로그인이 완료되었습니다."),
    PHONE_VERIFICATION_SENT(HttpStatus.OK,
            "AUTH200_PHONE_SENT",
            "인증번호가 발송되었습니다."),
    PHONE_VERIFIED(HttpStatus.OK,
            "AUTH200_PHONE_VERIFIED",
            "전화번호 인증이 완료되었습니다."),
    GEOCODED(HttpStatus.OK,
            "AUTH200_GEOCODED",
            "주소 좌표 조회가 완료되었습니다."),
    SIGNUP_COMPLETED(HttpStatus.CREATED,
            "AUTH201_SIGNUP",
            "회원가입이 완료되었습니다."),
    SIGNUP_REQUIRED(HttpStatus.OK,
            "AUTH200_SIGNUP_REQUIRED",
            "추가 회원가입이 필요합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
