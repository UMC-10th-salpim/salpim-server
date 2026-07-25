package salpim.umc10thsalpim.domain.term.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class AgreementException extends ProjectException {

    public AgreementException(BaseErrorCode code) {
        super(code);
    }
}
