package salpim.umc10thsalpim.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // --- 400 Bad Request ---
    // 휴대폰 / SMS 인증
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "인증번호가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST,
            "AUTH400_2",
            "인증번호가 만료되었습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST,
            "AUTH400_3",
            "휴대폰 인증이 완료되지 않았습니다."),
    PHONE_CHANGE_REQUEST_INVALID(HttpStatus.BAD_REQUEST,
            "AUTH400_4",
            "전화번호와 인증 토큰은 함께 입력해야 합니다."),
    INVALID_PHONE_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST,
            "AUTH400_5",
            "유효하지 않은 전화번호 변경 인증 토큰입니다."),
    EXPIRED_PHONE_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST,
            "AUTH400_6",
            "전화번호 변경 인증 토큰이 만료되었습니다."),

    // 카카오 OAuth
    KAKAO_AUTHORIZATION_CODE_INVALID(HttpStatus.BAD_REQUEST,
            "AUTH400_7",
            "카카오 인가 코드가 유효하지 않습니다."),
    KAKAO_PHONE_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST,
            "AUTH400_8",
            "카카오에서 전화번호를 제공하지 않아 전화번호 인증이 필요합니다."),

    // 비밀번호 재설정
    PASSWORD_RESET_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST,
            "AUTH400_9",
            "전화번호 또는 비밀번호 복구 답변이 일치하지 않습니다."),

    // 약관 동의
    TERMS_AGREEMENT_NOT_SUBMITTED(HttpStatus.BAD_REQUEST,
            "AUTH400_10",
            "약관 동의 정보가 제출되지 않았습니다."),
    TERMS_AGREEMENT_EXPIRED(HttpStatus.BAD_REQUEST,
            "AUTH400_11",
            "약관 동의가 만료되었습니다. 약관 동의를 다시 제출해 주세요."),
    TERMS_AGREEMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST,
            "AUTH400_12",
            "약관 동의 정보를 확인할 수 없습니다. 약관 동의를 다시 제출해 주세요."),

    // --- 401 Unauthorized ---
    // 비밀번호 / 로그인 자격증명
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,
            "AUTH401_1",
            "비밀번호가 일치하지 않습니다."),
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED,
            "AUTH401_2",
            "전화번호 또는 비밀번호가 올바르지 않습니다."),

    // JWT / 인증 토큰
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_3",
            "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_4",
            "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,
            "AUTH401_5",
            "리프레시 토큰이 만료되었습니다."),

    // 회원가입 토큰
    SIGNUP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_6",
            "유효하지 않은 회원가입 토큰입니다."),
    SIGNUP_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "AUTH401_7",
            "회원가입 토큰이 만료되었습니다."),
    SIGNUP_TOKEN_TYPE_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_8",
            "회원가입 토큰이 아닙니다."),

    // 비밀번호 재설정 토큰
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_9",
            "유효하지 않은 비밀번호 초기화 인증 토큰입니다."),
    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,
            "AUTH401_10",
            "비밀번호 초기화 인증 토큰이 만료되었습니다."),
    PASSWORD_RESET_TOKEN_TYPE_INVALID(HttpStatus.UNAUTHORIZED,
            "AUTH401_11",
            "비밀번호 초기화 토큰이 아닙니다."),

    // --- 404 Not Found ---
    LOGIN_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH404_1",
            "등록되지 않은 회원입니다."),
    GEOCODING_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "AUTH404_2",
            "주소 검색 결과를 찾을 수 없습니다."),

    // --- 429 Too Many Requests ---
    LOGIN_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_1",
            "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해 주세요."),
    PHONE_VERIFICATION_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_2",
            "인증번호는 1분 후에 다시 요청할 수 있습니다."),
    PHONE_VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_3",
            "인증번호 검증 시도 횟수를 초과했습니다. 잠시 후 다시 시도해 주세요."),
    PASSWORD_VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_4",
            "비밀번호 검증 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),

    // --- 502 Bad Gateway ---
    GEOCODING_API_ERROR(HttpStatus.BAD_GATEWAY,
            "AUTH502_1",
            "주소 좌표 조회에 실패했습니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY,
            "AUTH502_2",
            "카카오 API 요청에 실패했습니다."),
    KAKAO_USER_ID_MISSING(HttpStatus.BAD_GATEWAY,
            "AUTH502_3",
            "카카오 사용자 식별자를 확인할 수 없습니다."),
    SMS_SEND_FAILED(HttpStatus.BAD_GATEWAY,
            "AUTH502_4",
            "인증번호 문자 발송에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
