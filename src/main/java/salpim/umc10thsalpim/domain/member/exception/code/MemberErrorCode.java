package salpim.umc10thsalpim.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    REQUIRED_LOCAL_PASSWORD(HttpStatus.BAD_REQUEST,
            "MEMBER400_1",
            "로컬 회원은 비밀번호가 필요합니다."),
    REQUIRED_LOCAL_PHONE_NUMBER(HttpStatus.BAD_REQUEST,
            "MEMBER400_2",
            "로컬 회원은 전화번호가 필요합니다."),
    REQUIRED_PASSWORD_RECOVERY_ANSWER(HttpStatus.BAD_REQUEST,
            "MEMBER400_3",
            "로컬 회원은 비밀번호 복구 답변이 필요합니다."),
    REQUIRED_KAKAO_ID(HttpStatus.BAD_REQUEST,
            "MEMBER400_4",
            "카카오 회원은 카카오 ID가 필요합니다."),
    MEMBER_REGION_NOT_SET(HttpStatus.BAD_REQUEST,
            "MEMBER400_5",
            "회원의 지역 정보가 설정되지 않았습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST,
            "MEMBER400_6",
            "현재 비밀번호가 일치하지 않습니다."),
    RECOVERY_ANSWER_MISMATCH(HttpStatus.BAD_REQUEST,
            "MEMBER400_7",
            "비밀번호 찾기 답변이 일치하지 않습니다."),
    INVALID_PASSWORD_VERIFICATION(HttpStatus.BAD_REQUEST,
            "MEMBER400_8",
            "비밀번호 변경에 필요한 검증 정보가 올바르지 않습니다."),
    PASSWORD_CHANGE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST,
            "MEMBER400_9",
            "카카오 로그인 회원은 비밀번호를 변경할 수 없습니다."),
    PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST,
            "MEMBER400_10",
            "새 비밀번호는 현재 비밀번호와 달라야 합니다."),

    DUPLICATE_KAKAO_ACCOUNT(HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 가입된 카카오 계정입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT,
            "MEMBER409_2",
            "이미 가입된 전화번호입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "MEMBER404_1",
            "해당 사용자를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
