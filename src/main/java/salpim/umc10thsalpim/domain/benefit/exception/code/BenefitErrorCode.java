package salpim.umc10thsalpim.domain.benefit.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum BenefitErrorCode implements BaseErrorCode {

    BENEFIT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "BENEFIT404",
            "해당 복지 혜택을 찾을 수 없습니다."),
    BENEFIT_RULE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "BENEFIT_RULE404",
            "해당 혜택의 신청 규칙을 찾을 수 없습니다."),
    BENEFIT_REGION_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_1",
            "해당 혜택의 지역 조건 정보가 설정되지 않았습니다."),
    BENEFIT_REGION_LEVEL_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR,
            "BENEFIT500_2",
            "혜택의 지역이 신청 규칙의 지역 범위와 일치하지 않습니다.")
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
