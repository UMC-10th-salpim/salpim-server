package salpim.umc10thsalpim.domain.map.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;
import tools.jackson.core.ObjectReadContext;

public class MapException extends ProjectException {
    public MapException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
