package salpim.umc10thsalpim.global.apiPayload.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public class ProjectException extends RuntimeException {
    private final BaseErrorCode errorCode;
}
