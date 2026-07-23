package salpim.umc10thsalpim.global.infra.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class BokjiroException extends ProjectException {
    public BokjiroException(BaseErrorCode code) {
        super(code);
    }
}
