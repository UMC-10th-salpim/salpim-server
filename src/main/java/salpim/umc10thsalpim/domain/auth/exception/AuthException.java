package salpim.umc10thsalpim.domain.auth.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class AuthException extends ProjectException {

    public AuthException(BaseErrorCode code) {
        super(code);
    }
}
