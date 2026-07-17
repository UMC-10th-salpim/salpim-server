package salpim.umc10thsalpim.domain.region.exception;

import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

public class RegionException extends ProjectException {
    public RegionException(BaseErrorCode code) {
        super(code);
    }
}
