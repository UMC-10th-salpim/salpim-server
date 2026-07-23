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
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
