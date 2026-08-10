package salpim.umc10thsalpim.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthSuccessCode;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.exception.code.MemberSuccessCode;
import salpim.umc10thsalpim.domain.member.service.MemberService;
import salpim.umc10thsalpim.domain.member.service.MemberWithdrawalService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberWithdrawalService memberWithdrawalService;
    private final MemberService memberService;
    private final PhoneVerificationService phoneVerificationService;

    @Operation(
            summary = "회원 탈퇴 API",
            description = "Bearer Access Token으로 인증된 현재 회원과 회원 소유 인증·약관 동의 데이터를 삭제합니다. 탈퇴 후 기존 토큰은 사용할 수 없습니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @DeleteMapping("/members/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long memberId
    ) {
        memberWithdrawalService.withdraw(memberId);
        return ResponseEntity.status(GeneralSuccessCode.DELETED.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.DELETED, null));
    }

    @GetMapping("/users/me")
    @Operation(
            summary = "마이페이지 조회",
            description = "회원 기본정보 및 개인정보 수정용 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<MemberResDTO.MyPageInfo> getMyPage(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_MY_PAGE_VIEW,
                memberService.getMyPage(memberId)
        );
    }

    @GetMapping("/users/me/welfare-center")
    @Operation(
            summary = "소속 복지관 조회",
            description = "회원가입 시 선택한 지역(regionId)을 기준으로 배정된 복지관 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<MemberResDTO.WelfareCenterInfo> getWelfareCenter(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_WELFARE_CENTER_VIEW,
                memberService.getWelfareCenter(memberId)
        );
    }

    @PutMapping("/users/me")
    @Operation(
            summary = "개인정보 수정",
            description = "회원의 이름, 생년월일, 성별, 주소, 거주 지역 정보를 수정합니다. "
                    + "전화번호를 변경하지 않으면 phoneNumber와 phoneVerificationToken을 보내지 않습니다. "
                    + "전화번호 변경 시 두 값을 함께 전달해야 합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberReqDTO.UpdateProfile request
    ) {
        memberService.updateProfile(memberId, request);

        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_PROFILE_UPDATED,
                null
        );
    }

    @PostMapping("/users/me/phone-verification/send")
    @Operation(
            summary = "전화번호 변경 인증번호 발송",
            description = "변경할 전화번호로 인증번호를 발송합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ResponseEntity<ApiResponse<Void>> sendPhoneChangeVerificationCode(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody AuthReqDTO.PhoneSend request
    ) {
        phoneVerificationService.sendPhoneChangeVerificationCode(
                memberId,
                request.phoneNumber()
        );

        return ResponseEntity.status(AuthSuccessCode.PHONE_VERIFICATION_SENT.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.PHONE_VERIFICATION_SENT, null));
    }

    @PostMapping("/users/me/phone-verification/verify")
    @Operation(
            summary = "전화번호 변경 인증번호 검증",
            description = "인증번호를 검증하고 개인정보 수정에 사용할 인증 토큰을 발급합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ResponseEntity<ApiResponse<AuthResDTO.PhoneChangeVerifyResult>> verifyPhoneChangeCode(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody AuthReqDTO.PhoneVerify request
    ) {
        AuthResDTO.PhoneChangeVerifyResult response = phoneVerificationService
                .verifyPhoneChangeCode(
                        memberId,
                        request.phoneNumber(),
                        request.code()
                );

        return ResponseEntity.status(AuthSuccessCode.PHONE_VERIFIED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.PHONE_VERIFIED, response));
    }

    @PostMapping("/users/me/password/verify")
    @Operation(
            summary = "현재 비밀번호 확인",
            description = "입력한 현재 비밀번호가 회원 정보와 일치하는지 확인합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<MemberResDTO.PasswordVerificationResult> verifyCurrentPassword(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberReqDTO.VerifyCurrentPassword request
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_PASSWORD_VERIFIED,
                memberService.verifyCurrentPassword(memberId, request)
        );
    }

    @PostMapping("/users/me/password/recovery/verify")
    @Operation(
            summary = "비밀번호 복구 답변 확인",
            description = "비밀번호 찾기 답변이 회원 정보와 일치하는지 확인합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<MemberResDTO.PasswordVerificationResult> verifyRecoveryAnswer(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberReqDTO.VerifyRecoveryAnswer request
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_PASSWORD_VERIFIED,
                memberService.verifyRecoveryAnswer(memberId, request)
        );
    }

    @PutMapping("/users/me/password")
    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호 또는 복구 답변을 재검증한 뒤 새 비밀번호로 변경합니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberReqDTO.ChangePassword request
    ) {
        memberService.changePassword(memberId, request);

        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_PASSWORD_CHANGED,
                null
        );
    }
}
