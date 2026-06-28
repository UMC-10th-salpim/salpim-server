package salpim.umc10thsalpim.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralErrorCode;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;
// BaseSuccessCode 구현체 (GeneralSuccessCode 등)가 있다면 import 해주세요!

@RestController
@RequestMapping("/test")
@Tag(name = "Test API", description = "공통 응답 및 에러 핸들링 테스트용 API")
public class TestController {

    // 1. 정상 성공 테스트
    @Operation(summary = "성공 응답 테스트", description = "정상적인 ApiResponse 형태를 반환합니다.")
    @GetMapping("/success")
    public ApiResponse<String> testSuccess() {
        // TODO: 만들어둔 SuccessCode 구현체가 있다면 그걸 사용해주세요! (예: GeneralSuccessCode.OK)
        // 만약 임시로 테스트한다면 파라미터에 null을 넣거나 임시 객체를 생성해서 넣으세요.
    return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, "테스트 데이터가 성공적으로 생성되었습니다.");
        //return null; // 본인의 SuccessCode 구현체에 맞게 수정해주세요!
    }

    // 2. 커스텀 예외 테스트 (ProjectException)
    @Operation(summary = "커스텀 에러 테스트", description = "ProjectException을 강제로 발생시킵니다.")
    @GetMapping("/error/custom")
    public ApiResponse<Void> testCustomError() {
        // 우리가 정의한 BAD_REQUEST 에러를 던짐
        throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
    }

    // 3. 최상위 시스템 예외 테스트 (500 Internal Server Error)
    @Operation(summary = "서버 500 에러 테스트", description = "예상치 못한 Exception을 강제로 발생시킵니다.")
    @GetMapping("/error/internal")
    public ApiResponse<Void> testInternalError() {
        // 일부러 NullPointerException을 유도하여 500 에러 핸들러가 작동하는지 확인
        String str = null;
        str.length();
        return null;
    }

    // 4. @Valid 유효성 검사 실패 테스트 (MethodArgumentNotValidException)
    @Operation(summary = "Validation 에러 테스트", description = "DTO 검증 실패 시 Map 형태로 에러를 반환하는지 확인합니다.")
    @PostMapping("/error/validation")
    public ApiResponse<String> testValidation(@Valid @RequestBody TestDto request) {
        return null; // 성공 코드 객체로 수정해주세요!
    }

    // Validation 테스트용 내부 DTO
    @Getter
    @Setter
    public static class TestDto {
        @NotBlank(message = "이름은 필수 입력값입니다. 비워둘 수 없습니다.")
        private String name;
    }
}

