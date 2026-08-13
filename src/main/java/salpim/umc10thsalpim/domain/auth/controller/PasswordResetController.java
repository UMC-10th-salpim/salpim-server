package salpim.umc10thsalpim.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthSuccessCode;
import salpim.umc10thsalpim.domain.auth.service.PasswordResetService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

@Tag(name = "비밀번호 재설정", description = "로그인하지 않은 상태에서 비밀번호를 재설정하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 복구 답변 검증 및 재설정 토큰 발급",
        description = """
            전화번호와 비밀번호 복구 질문 답변을 검증하고, 통과하면 비밀번호 재설정 토큰을 발급합니다.
            답변은 앞뒤 공백만 제거한 뒤 비교하므로 중간 띄어쓰기까지 회원가입 시 입력한 것과 같아야 합니다.
            - passwordResetToken: 비밀번호 재설정 API에 그대로 전달합니다. 기본 설정 기준 5분간 유효하며 한 번만 사용할 수 있습니다.
            
            5회 연속 실패하면 15분간 잠기고 429로 응답합니다.
            """
    )
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResDTO.PasswordResetVerifyResult>> verifyRecoveryAnswer(
            @Valid @RequestBody AuthReqDTO.PasswordResetVerify request
    ){
        AuthResDTO.PasswordResetVerifyResult response =
                passwordResetService.verifyRecoveryAnswer(request);

        return ResponseEntity.status(AuthSuccessCode.PASSWORD_RESET_VERIFIED.getStatus())
                .body(ApiResponse.onSuccess(
                        AuthSuccessCode.PASSWORD_RESET_VERIFIED,
                        response
                ));
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = """
                발급받은 재설정 토큰으로 비밀번호를 변경하고 기존 로그인 세션을 무효화합니다.
                - passwordResetToken: 복구 답변 검증 API에서 받은 토큰입니다.
                - newPassword: 6자리 숫자이며, 현재 사용 중인 비밀번호와 같으면 400으로 응답합니다.
                
                재설정 토큰은 1회용이라 한 번 사용하면 다시 쓸 수 없고, 만료되었거나 이미 사용된 토큰은 401로 응답합니다.
                """
    )
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody AuthReqDTO.PasswordReset request
    ) {
        passwordResetService.resetPassword(request);

        return ResponseEntity.status(AuthSuccessCode.PASSWORD_RESET_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(
                        AuthSuccessCode.PASSWORD_RESET_COMPLETED,
                        null
                ));
    }
}
