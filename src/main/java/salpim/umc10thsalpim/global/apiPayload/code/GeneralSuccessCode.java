package salpim.umc10thsalpim.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK, "COMMON200", "요청이 성공적으로 완료되었습니다."),
    CREATED(HttpStatus.CREATED, "COMMON201", "성공적으로 생성되었습니다."),
    DELETED(HttpStatus.OK, "COMMON200_DELETE", "성공적으로 삭제되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "COMMON202", "요청이 정상적으로 접수되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
