package salpim.umc10thsalpim.domain.benefit.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class BenefitException extends ProjectException {
    public BenefitException(BaseErrorCode code) {
        super(code);
    }
}
