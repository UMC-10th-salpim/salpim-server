package salpim.umc10thsalpim.domain.region.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum RegionSuccessCode implements BaseSuccessCode {

    REGION_LIST_GET_SUCCESS(HttpStatus.OK,
            "REGION200_1",
            "지역 리스트의 조회가 성공적으로 완료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
