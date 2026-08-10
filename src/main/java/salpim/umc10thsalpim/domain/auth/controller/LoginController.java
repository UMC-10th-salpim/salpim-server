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
import salpim.umc10thsalpim.domain.auth.enums.NextStep;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthSuccessCode;
import salpim.umc10thsalpim.domain.auth.service.KakaoAuthService;
import salpim.umc10thsalpim.domain.auth.service.LocalLoginService;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

@Tag(name = "Login", description = "Login API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class LoginController {

    private final LocalLoginService localLoginService;
    private final KakaoAuthService kakaoAuthService;
    private final TokenService tokenService;

    @Operation(summary = "Local login API")
    @PostMapping("/local")
    public ResponseEntity<ApiResponse<AuthResDTO.TokenResult>> loginLocal(
            @Valid @RequestBody AuthReqDTO.LocalLogin request
    ) {
        AuthResDTO.TokenResult response = localLoginService.login(request);
        return ResponseEntity.status(AuthSuccessCode.LOGIN_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, response));
    }

    @Operation(summary = "Access and refresh token reissue API")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthResDTO.TokenResult>> reissueTokens(
            @Valid @RequestBody AuthReqDTO.TokenReissue request
    ) {
        AuthResDTO.TokenResult response = tokenService.reissueLoginTokens(request.refreshToken());
        return ResponseEntity.status(AuthSuccessCode.TOKEN_REISSUED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUED, response));
    }

    @Operation(summary = "Kakao login API")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<AuthResDTO.KakaoLoginResult>> loginKakao(
            @Valid @RequestBody AuthReqDTO.KakaoLogin request
    ) {
        AuthResDTO.KakaoLoginResult response = kakaoAuthService.login(request.authorizationCode());
        AuthSuccessCode successCode = response.nextStep() == NextStep.SIGNUP_REQUIRED
                ? AuthSuccessCode.SIGNUP_REQUIRED
                : AuthSuccessCode.LOGIN_SUCCESS;
        return ResponseEntity.status(successCode.getStatus())
                .body(ApiResponse.onSuccess(successCode, response));
    }
}
