package salpim.umc10thsalpim.domain.recommendation.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements BaseErrorCode {

    OPTION_ID_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMENDATION404_1",
            "유효하지 않은 선택지 id입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
