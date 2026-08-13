package salpim.umc10thsalpim.global.apiPayload.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseErrorCode;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralErrorCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    // 프로젝트에서 발생한 예외 처리
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberException(
            ProjectException e
    ) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, null));
    }
    //타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        Map<String, String> errors = new HashMap<>();
        errors.put(e.getName(), e.getName() + " 형식이 올바르지 않습니다.");

        return ResponseEntity.status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, errors));
    }
    //파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMissingRequestParameter(
            MissingServletRequestParameterException e
    ) {
        Map<String, String> errors = new HashMap<>();
        errors.put(e.getParameterName(), e.getParameterName() + " is required.");

        return ResponseEntity.status(GeneralErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.BAD_REQUEST, errors));
    }
    //지원하지 않는 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e
    ) {
        HttpHeaders headers = new HttpHeaders();
        if (e.getSupportedHttpMethods() != null) {
            headers.setAllow(e.getSupportedHttpMethods());
        }
        return ResponseEntity.status(GeneralErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .headers(headers)
                .body(ApiResponse.onFailure(GeneralErrorCode.METHOD_NOT_ALLOWED, null));
    }
    //없는 리소스 경로
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException e
    ) {
        return ResponseEntity.status(GeneralErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.onFailure(GeneralErrorCode.NOT_FOUND, null));
    }
    // 그 외의 정의되지 않은 모든 예외 처리 -> 서버 오류로 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(
            Exception ex
    ) {
        log.error("Unhandled server exception", ex);
        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }

    // @Valid 어노테이션 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        // 검증 실패한 변수명과 실패 이유를 담을 Map
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(
                error -> {errors.put(error.getField(), error.getDefaultMessage());
        });

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    // @RequestParam, @PathVariable 등 개별 파라미터 검증 실패 예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        // 검증 실패한 변수명과 실패 이유를 담을 Map
        Map<String, String> errors = new HashMap<>();

        e.getConstraintViolations().forEach(violation -> {
            // prop ertyPath는 "메서드명.파라미터명" 형태라서 마지막 부분만 파라미터명으로 사용
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        });

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;

        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.onFailure(code, null));
    }
}
