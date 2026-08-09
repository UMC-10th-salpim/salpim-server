package salpim.umc10thsalpim.domain.benefit.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum BenefitSuccessCode implements BaseSuccessCode {

    BENEFIT_VIEW(HttpStatus.OK,
            "BENEFIT200_1",
            "성공적으로 혜택을 조회했습니다"),
    BENEFIT_FAVORITE_ADD(HttpStatus.OK,
            "BENEFIT200_2",
            "혜택을 찜했습니다."),
    BENEFIT_FAVORITE_REMOVE(HttpStatus.OK,
            "BENEFIT200_3",
            "혜택 찜을 해제했습니다."),
    BENEFIT_SHARE_SUCCESS(HttpStatus.OK,
            "SHARE200_1",
            "성공적으로 공유하기용 혜택 정보를 조회했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
