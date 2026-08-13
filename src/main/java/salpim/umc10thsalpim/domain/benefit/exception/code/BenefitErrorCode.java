package salpim.umc10thsalpim.domain.benefit.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum BenefitErrorCode implements BaseErrorCode {

    BENEFIT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "BENEFIT404_1",
            "해당 복지 혜택을 찾을 수 없습니다."),
    BENEFIT_RULE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "BENEFIT404_2",
            "해당 혜택의 신청 규칙을 찾을 수 없습니다."),

    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST,
            "BENEFIT400_1",
            "유효하지 않은 정렬 방식입니다."),
    BENEFIT_ONLINE_APPLICATION_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,
            "BENEFIT400_2",
            "해당 혜택은 온라인 신청을 지원하지 않습니다."),

    BENEFIT_REGION_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_1",
            "해당 혜택의 지역 조건 정보가 설정되지 않았습니다."),
    BENEFIT_REGION_LEVEL_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_2",
            "혜택의 지역이 지역 범위와 일치하지 않습니다."),
    BENEFIT_APPLICATION_URL_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_3",
            "해당 혜택의 온라인 신청 URL이 설정되지 않았습니다."),
    BENEFIT_APPLICATION_URL_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_4",
            "해당 혜택의 온라인 신청 URL 형식이 올바르지 않습니다."),
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
