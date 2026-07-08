package salpim.umc10thsalpim.domain.welfare.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class WelfareException extends ProjectException {
    public WelfareException(BaseErrorCode code) {
        super(code);
    }
}
