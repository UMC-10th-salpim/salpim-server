package salpim.umc10thsalpim.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthSuccessCode;
import salpim.umc10thsalpim.domain.auth.service.PasswordResetService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

@Tag(name = "Password Reset", description = "비로그인 비밀번호 재설정")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 복구 답변 검증 및 재설정 토큰 발급")
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
}
