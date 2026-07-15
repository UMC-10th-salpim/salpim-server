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
    REQUIRED_LOCAL_PASSWORD(HttpStatus.BAD_REQUEST,
            "MEMBER400_PASSWORD_REQUIRED",
            "로컬 회원은 비밀번호가 필요합니다."),
    REQUIRED_PASSWORD_RECOVERY_ANSWER(HttpStatus.BAD_REQUEST,
            "MEMBER400_PASSWORD_ANSWER_REQUIRED",
            "로컬 회원은 비밀번호 복구 답변이 필요합니다."),
    REQUIRED_KAKAO_ID(HttpStatus.BAD_REQUEST,
            "MEMBER400_KAKAO_ID_REQUIRED",
            "카카오 회원은 카카오 ID가 필요합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
