package salpim.umc10thsalpim.domain.recommendation.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum RecommendationSuccessCode implements BaseSuccessCode {

    OPTION_LIST_GET_SUCCESS(
            HttpStatus.OK,
            "RECOMMENDATION200_1",
            "선택지들의 정보 조회가 성공적으로 완료되었습니다."
    ),
    RECOMMENDATION_GET_SUCCESS(
            HttpStatus.OK,
            "RECOMMENDATION200_2",
            "살피미 추천 혜택 리스트 조회가 성공적으로 완료되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
