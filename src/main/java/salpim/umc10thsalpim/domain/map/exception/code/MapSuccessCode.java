package salpim.umc10thsalpim.domain.map.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum MapSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK,
            "COMMON200",
            "시설 정보를 조회했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
