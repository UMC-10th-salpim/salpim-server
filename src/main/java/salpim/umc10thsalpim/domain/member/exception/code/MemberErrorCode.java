package salpim.umc10thsalpim.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    REQUIRED_LOCAL_PASSWORD(HttpStatus.BAD_REQUEST,
            "MEMBER400_PASSWORD_REQUIRED",
            "로컬 회원은 비밀번호가 필요합니다."),
    REQUIRED_PASSWORD_RECOVERY_ANSWER(HttpStatus.BAD_REQUEST,
            "MEMBER400_PASSWORD_ANSWER_REQUIRED",
            "로컬 회원은 비밀번호 복구 답변이 필요합니다."),
    REQUIRED_KAKAO_ID(HttpStatus.BAD_REQUEST,
            "MEMBER400_KAKAO_ID_REQUIRED",
            "카카오 회원은 카카오 ID가 필요합니다."),
    DUPLICATE_KAKAO_ACCOUNT(HttpStatus.CONFLICT,
            "MEMBER409_KAKAO",
            "이미 가입된 카카오 계정입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT,
            "MEMBER409_PHONE",
            "이미 가입된 전화번호입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_NOT_FOUND",
            "해당 사용자를 찾을 수 없습니다."),
    MEMBER_REGION_NOT_SET(HttpStatus.BAD_REQUEST,
            "MEMBER400_1",
            "회원의 지역 정보가 설정되지 않았습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
