package salpim.umc10thsalpim.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

@Tag(name = "로그인", description = "로컬/카카오 로그인 및 토큰 재발급 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class LoginController {

    private final LocalLoginService localLoginService;
    private final KakaoAuthService kakaoAuthService;
    private final TokenService tokenService;

    @Operation(summary = "로컬 로그인",
    description = """
                전화번호와 비밀번호로 로그인하고 액세스/리프레시 토큰을 발급합니다.
                전화번호는 하이픈 유무와 관계없이 숫자만 남겨 비교합니다.
                - accessToken: 인증이 필요한 API의 Authorization 헤더에 'Bearer {accessToken}' 형식으로 담아 보냅니다.
                - refreshToken: 액세스 토큰이 만료되면 토큰 재발급 API에 사용합니다.
                - wordSize: 회원이 설정한 글씨 크기이며, 로그인 직후 화면에 바로 적용할 수 있습니다.
                
                로그인 실패가 누적되면(기본 설정 기준 전화번호 5회 / IP 15회) 일정 시간 동안 429로 차단됩니다.
                """
    )
    @PostMapping("/local")
    public ResponseEntity<ApiResponse<AuthResDTO.TokenResult>> loginLocal(
            @Valid @RequestBody AuthReqDTO.LocalLogin request,
            HttpServletRequest httpServletRequest
    ) {
        AuthResDTO.TokenResult response = localLoginService.login(
                request,
                httpServletRequest.getRemoteAddr()
        );
        return ResponseEntity.status(AuthSuccessCode.LOGIN_SUCCESS.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, response));
    }

    @Operation(summary = "액세스/리프레시 토큰 재발급",
            description = """
                리프레시 토큰으로 액세스 토큰과 리프레시 토큰을 함께 재발급합니다.
                재발급 시 리프레시 토큰도 교체되므로, 응답으로 받은 refreshToken을 반드시 저장하고 이전 토큰은 폐기해야 합니다.
                회원당 리프레시 토큰은 1개만 유지되어, 다른 기기에서 로그인하거나 재발급하면 이전 토큰은 사용할 수 없습니다.
                저장된 토큰과 일치하지 않는 리프레시 토큰이 들어오면 서버에 저장된 토큰까지 삭제되므로 다시 로그인해야 합니다.
                유효하지 않거나 만료된 토큰은 401로 응답합니다.
                """
    )
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<AuthResDTO.TokenResult>> reissueTokens(
            @Valid @RequestBody AuthReqDTO.TokenReissue request
    ) {
        AuthResDTO.TokenResult response = tokenService.reissueLoginTokens(request.refreshToken());
        return ResponseEntity.status(AuthSuccessCode.TOKEN_REISSUED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUED, response));
    }

    @Operation(summary = "카카오 로그인",
            description = """
                카카오 인가 코드로 로그인하고, 기존 회원 여부에 따라 다음 단계를 안내합니다.
                두 경우 모두 HTTP 200이며 응답 코드로 구분됩니다. (로그인 완료: AUTH200_LOGIN / 추가 회원가입 필요: AUTH200_SIGNUP_REQUIRED)
                - nextStep: LOGIN_COMPLETE면 로그인이 완료된 상태이고, SIGNUP_REQUIRED면 카카오 회원가입 API를 이어서 호출해야 합니다.
                - isNewMember: 가입 이력이 없는 카카오 계정이면 true입니다.
                - accessToken / refreshToken / wordSize: LOGIN_COMPLETE인 경우에만 내려옵니다.
                - signupToken: SIGNUP_REQUIRED인 경우에만 내려오며, 카카오 회원가입 API의 Authorization 헤더에 'Bearer {signupToken}' 형식으로 담아 보냅니다. 기본 설정 기준 10분간 유효합니다.
                - phoneVerificationRequired: true면 카카오에서 전화번호를 받지 못한 경우로, 회원가입 전에 전화번호 인증과 약관 동의 제출을 먼저 진행해야 합니다.
                - phoneNumber: 카카오에서 전화번호를 받은 경우에만 내려옵니다.
                """
    )
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
