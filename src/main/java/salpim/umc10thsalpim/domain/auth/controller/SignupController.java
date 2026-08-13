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

@Tag(name = "회원가입", description = "회원가입 관련 API")
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

    @Operation(
            summary = "전화번호 인증번호 발송",
            description = """
                회원가입용 6자리 인증번호를 SMS로 발송합니다. 인증번호는 발송 후 5분간 유효합니다.
                이미 가입된 전화번호로는 발송할 수 없으며 409로 응답합니다.
                같은 번호로는 60초 이내에 다시 발송할 수 없고, 이 경우 429로 응답합니다.
                재발송하면 이전 인증번호는 사용할 수 없고, 이미 제출한 약관 동의도 무효가 되어 다시 제출해야 합니다.
                SMS 발송 자체가 실패하면 502로 응답합니다.
                """
    )
    @PostMapping("/phone/send")
    public ResponseEntity<ApiResponse<Void>> sendPhoneVerificationCode(
            @Valid @RequestBody AuthReqDTO.PhoneSend request
    ) {
        phoneVerificationService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.status(AuthSuccessCode.PHONE_VERIFICATION_SENT.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.PHONE_VERIFICATION_SENT, null));
    }

    @Operation(
            summary = "전화번호 인증번호 검증",
            description = """
                발송된 인증번호를 검증합니다. 성공하면 해당 전화번호는 인증 완료 상태가 되고, 회원가입이 끝날 때까지 유지됩니다.
                - verified: 검증에 성공하면 true입니다. 실패하는 경우에는 성공 응답 대신 에러로 내려갑니다.
                
                인증번호가 일치하지 않거나 만료(발송 후 5분)되었으면 400으로 응답합니다.
                5회 연속 실패하면 15분간 잠기고 429로 응답합니다.
                """
    )    @PostMapping("/phone/verify")
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

    @Operation(
            summary = "도로명 주소 좌표 조회",
            description = """
                도로명 주소를 카카오 주소 검색으로 조회해 좌표를 반환합니다.
                여기서 받은 latitude / longitude를 회원가입 요청에 그대로 담아 보냅니다.
                - roadAddress: 앞뒤 공백을 제거한 요청 주소를 그대로 돌려줍니다.
                
                검색 결과가 없으면 404, 카카오 API 호출이 실패하면 502로 응답합니다.
                """
    )
    @PostMapping("/location/geocode")
    public ResponseEntity<ApiResponse<AuthResDTO.GeocodeResult>> geocode(
            @Valid @RequestBody AuthReqDTO.Geocode request
    ) {
        AuthResDTO.GeocodeResult response = geocodingService.geocode(request.roadAddress());
        return ResponseEntity.status(AuthSuccessCode.GEOCODED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.GEOCODED, response));
    }

    @Operation(
            summary = "회원가입 약관 동의 제출",
            description = """
                전화번호 인증이 완료된 번호에 대해 필수 약관 동의 여부를 검증하고,
                전화번호 인증 해시와 약관 동의 정보를 묶은 별개의 해시를 저장합니다.
                이후 회원가입 API 호출 시 두 해시가 모두 검증되어야 회원이 생성됩니다.
                - agreements: 약관 조회 API에서 받은 약관 버전 ID와 동의 여부 목록입니다. 필수 약관은 모두 true여야 합니다.
                
                동의 정보는 제출 후 5분간만 유효하며, 만료되면 다시 제출해야 합니다.
                제출 이후 인증번호를 다시 발송하면 동의 정보가 무효가 되므로 약관 동의부터 다시 진행해야 합니다.
                전화번호 인증이 완료되지 않았거나 필수 약관에 동의하지 않았으면 400으로 응답합니다.
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
            summary = "로컬 회원가입",
            description = """
                전화번호 인증과 약관 동의 제출이 모두 끝난 뒤에 호출해야 하며, 성공 시 201로 응답합니다.
                - password: 6자리 숫자입니다.
                - passwordAnswer: 비밀번호 복구 질문의 답변이며, 비밀번호 재설정 시 본인 확인에 사용됩니다.
                - regionId: 행정구역 조회 API에서 발급받은 최하위(읍/면/동) 지역 ID이며, 해당 이름이 회원의 welfareCenter에 저장됩니다.
                - latitude / longitude: 도로명 주소 좌표 조회 API의 응답 값을 그대로 전달합니다.
                - detailAddress: 상세 주소로 생략할 수 있습니다.
                
                전화번호 인증이나 약관 동의가 확인되지 않으면 400, 이미 가입된 전화번호면 409로 응답합니다.
                """
    )
    @PostMapping("/local")
    public ResponseEntity<ApiResponse<Void>> signupLocal(
            @Valid @RequestBody AuthReqDTO.LocalSignup request
    ) {
        localSignupService.signup(request);
        return ResponseEntity.status(AuthSuccessCode.SIGNUP_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_COMPLETED, null));
    }

    @Operation(
            summary = "카카오 회원가입",
            description = """
                카카오 로그인 응답이 SIGNUP_REQUIRED일 때 발급받은 signupToken으로 회원가입을 완료합니다. 성공 시 201로 응답합니다.
                액세스 토큰이 아니라 signupToken을 Authorization 헤더에 담아야 합니다.
                - phoneNumber: 카카오 로그인 응답의 phoneVerificationRequired가 true인 경우에만 필요합니다. false이면 카카오에서 받은 번호가 사용되고 요청 값은 무시됩니다.
                - regionId: 행정구역 조회 API에서 발급받은 최하위(읍/면/동) 지역 ID입니다.
                
                phoneVerificationRequired가 true였다면 전화번호 인증과 약관 동의 제출을 먼저 끝내야 합니다.
                signupToken이 없거나 유효하지 않거나 만료되었으면 401, 이미 가입된 카카오 계정이거나 전화번호면 409로 응답합니다.
                """
    )    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<Void>> signupKakao(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody AuthReqDTO.KakaoSignup request
    ) {
        kakaoAuthService.signup(authorizationHeader, request);
        return ResponseEntity.status(AuthSuccessCode.SIGNUP_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_COMPLETED, null));
    }
}
