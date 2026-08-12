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
import salpim.umc10thsalpim.domain.auth.service.GeocodingService;
import salpim.umc10thsalpim.domain.auth.service.KakaoAuthService;
import salpim.umc10thsalpim.domain.auth.service.LocalSignupService;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.auth.service.SignupValidationService;
import salpim.umc10thsalpim.domain.auth.service.TermsAgreementVerificationService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

@Tag(name = "Signup", description = "회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup")
public class SignupController {

    private final PhoneVerificationService phoneVerificationService;
    private final GeocodingService geocodingService;
    private final LocalSignupService localSignupService;
    private final KakaoAuthService kakaoAuthService;
    private final SignupValidationService signupValidationService;
    private final TermsAgreementVerificationService termsAgreementVerificationService;

    @Operation(summary = "전화번호 인증번호 발송 API")
    @PostMapping("/phone/send")
    public ResponseEntity<ApiResponse<Void>> sendPhoneVerificationCode(
            @Valid @RequestBody AuthReqDTO.PhoneSend request
    ) {
        phoneVerificationService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.status(AuthSuccessCode.PHONE_VERIFICATION_SENT.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.PHONE_VERIFICATION_SENT, null));
    }

    @Operation(summary = "전화번호 인증번호 검증 API")
    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse<AuthResDTO.PhoneVerifyResult>> verifyPhoneCode(
            @Valid @RequestBody AuthReqDTO.PhoneVerify request
    ) {
        AuthResDTO.PhoneVerifyResult response = phoneVerificationService.verifyCode(
                request.phoneNumber(),
                request.code()
        );
        return ResponseEntity.status(AuthSuccessCode.PHONE_VERIFIED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.PHONE_VERIFIED, response));
    }

    @Operation(summary = "도로명 주소 좌표 조회 API")
    @PostMapping("/location/geocode")
    public ResponseEntity<ApiResponse<AuthResDTO.GeocodeResult>> geocode(
            @Valid @RequestBody AuthReqDTO.Geocode request
    ) {
        AuthResDTO.GeocodeResult response = geocodingService.geocode(request.roadAddress());
        return ResponseEntity.status(AuthSuccessCode.GEOCODED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.GEOCODED, response));
    }

    @Operation(
            summary = "회원가입 약관 동의 제출 API",
            description = """
                전화번호 인증이 완료된 번호에 대해 필수 약관 동의 여부를 검증하고,
                전화번호 인증 해시와 약관 동의 정보를 묶은 별개의 해시를 저장합니다.
                이후 회원가입 API 호출 시 두 해시가 모두 검증되어야 회원이 생성됩니다.
                """
    )
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<Void>> submitSignupTermsAgreement(
            @Valid @RequestBody AuthReqDTO.SignupTermsAgreement request
    ) {
        String normalizedPhoneNumber = signupValidationService.normalizePhoneNumber(request.phoneNumber());
        termsAgreementVerificationService.submitAgreement(normalizedPhoneNumber, request.agreements());
        return ResponseEntity.status(AuthSuccessCode.SIGNUP_TERMS_AGREEMENT_SUBMITTED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_TERMS_AGREEMENT_SUBMITTED, null));
    }

    @Operation(
            summary = "로컬 회원가입 API",
            description = "행정구역 조회 API에서 발급받은 regionId를 전달하면, 해당 읍·면·동 이름을 회원의 welfareCenter에 저장합니다."
    )
    @PostMapping("/local")
    public ResponseEntity<ApiResponse<Void>> signupLocal(
            @Valid @RequestBody AuthReqDTO.LocalSignup request
    ) {
        localSignupService.signup(request);
        return ResponseEntity.status(AuthSuccessCode.SIGNUP_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_COMPLETED, null));
    }

    @Operation(summary = "카카오 회원가입 API")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<Void>> signupKakao(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody AuthReqDTO.KakaoSignup request
    ) {
        kakaoAuthService.signup(authorizationHeader, request);
        return ResponseEntity.status(AuthSuccessCode.SIGNUP_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_COMPLETED, null));
    }
}
