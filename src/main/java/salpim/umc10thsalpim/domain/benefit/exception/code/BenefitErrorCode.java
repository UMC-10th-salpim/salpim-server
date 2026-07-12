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
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
