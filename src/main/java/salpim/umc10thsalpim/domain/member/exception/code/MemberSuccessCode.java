package salpim.umc10thsalpim.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    MEMBER_MY_PAGE_VIEW(HttpStatus.OK,
            "MEMBER200_1",
            "마이페이지 정보를 성공적으로 조회했습니다."),
    MEMBER_PROFILE_UPDATED(HttpStatus.OK,
            "MEMBER200_2",
            "회원 정보를 성공적으로 수정했습니다."),
    MEMBER_PASSWORD_VERIFIED(HttpStatus.OK,
            "MEMBER200_3",
            "비밀번호를 성공적으로 확인했습니다."),
    MEMBER_PASSWORD_CHANGED(HttpStatus.OK,
            "MEMBER200_4",
            "비밀번호를 성공적으로 변경했습니다."),
    MEMBER_WELFARE_CENTER_VIEW(HttpStatus.OK,
            "MEMBER200_5",
            "복지관 정보를 성공적으로 조회했습니다."),
    MEMBER_WORD_SIZE_UPDATED(HttpStatus.OK,
            "MEMBER200_6",
            "글자 크기를 성공적으로 수정했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
