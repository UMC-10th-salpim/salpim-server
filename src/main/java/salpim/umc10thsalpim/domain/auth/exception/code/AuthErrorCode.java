package salpim.umc10thsalpim.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    LOGIN_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH404_LOGIN_MEMBER",
            "등록되지 않은 회원입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,
            "AUTH401_PASSWORD",
            "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_TOKEN",
            "유효하지 않은 토큰입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_VERIFICATION",
            "인증번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_VERIFICATION_EXPIRED",
            "인증번호가 만료되었습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST,
            "AUTH400_PHONE_NOT_VERIFIED",
            "휴대폰 인증이 완료되지 않았습니다."),
    GEOCODING_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH404_GEOCODING",
            "주소 검색 결과를 찾을 수 없습니다."),
    GEOCODING_API_ERROR(HttpStatus.BAD_GATEWAY,
            "AUTH502_GEOCODING",
            "주소 좌표 조회에 실패했습니다."),
    KAKAO_AUTHORIZATION_CODE_INVALID(HttpStatus.BAD_REQUEST,
            "AUTH400_KAKAO_CODE",
            "카카오 인가 코드가 유효하지 않습니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY,
            "AUTH502_KAKAO",
            "카카오 API 요청에 실패했습니다."),
    KAKAO_USER_ID_MISSING(HttpStatus.BAD_GATEWAY,
            "AUTH502_KAKAO_ID",
            "카카오 사용자 식별자를 확인할 수 없습니다."),
    SIGNUP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_SIGNUP_TOKEN",
            "유효하지 않은 회원가입 토큰입니다."),
    SIGNUP_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "AUTH401_SIGNUP_TOKEN_EXPIRED",
            "회원가입 토큰이 만료되었습니다."),
    SIGNUP_TOKEN_TYPE_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_SIGNUP_TOKEN_TYPE",
            "회원가입 토큰이 아닙니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
