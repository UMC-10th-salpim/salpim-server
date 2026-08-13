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

@Tag(name = "회원", description = "마이페이지, 개인정보 수정, 비밀번호 변경 및 탈퇴 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberWithdrawalService memberWithdrawalService;
    private final MemberService memberService;
    private final PhoneVerificationService phoneVerificationService;

    @Operation(
            summary = "회원 탈퇴",
            description = """
                로그인한 회원과 회원이 소유한 데이터를 모두 삭제합니다.
                회원 정보와 함께 리프레시 토큰, 약관 동의 내역, 전화번호 인증 정보, 찜한 혜택,
                비밀번호 검증 실패 기록, 비밀번호 재설정 토큰이 함께 삭제됩니다.
                탈퇴 후에는 기존 액세스 토큰과 리프레시 토큰을 모두 사용할 수 없습니다.
                """,
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @DeleteMapping("/users/me")
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
            description = """
                회원 기본 정보와 개인정보 수정 화면에 필요한 정보를 함께 조회합니다.
                - regionId: 회원의 읍·면·동 지역 ID이며, 개인정보 수정 요청에 그대로 사용할 수 있습니다.
                - sido / sigungu / administrativeArea: 회원 지역의 시/도, 시/군/구, 읍/면/동 이름입니다.
                - generalGu: 일반구가 있는 지역만 값이 있고, 없으면 null입니다.
                - detailAddress: 상세 주소가 없으면 null입니다.
                - phoneNumber: 하이픈 없이 숫자만 저장된 값이 내려갑니다.
                
                회원의 지역 정보가 설정되어 있지 않으면 400, 지역을 찾을 수 없으면 404로 응답합니다.
                """
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
            description = """
                회원가입 또는 개인정보 수정에서 선택한 읍/면/동을 기준으로 배정된 복지관 정보를 조회합니다.
                - welfareCenter: 배정된 행정복지센터 이름이며, 설정되어 있지 않으면 null입니다.
                
                지도 API에서 선택한 시설이 본인 관할 센터인지 판별할 때 기준이 되는 값입니다.
                """,
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
            description = """
                회원의 이름, 생년월일, 성별, 주소, 거주 지역 정보를 수정합니다.
                전달한 값으로 전부 덮어쓰므로, 변경하지 않는 항목도 기존 값을 그대로 담아 보내야 합니다.
                - regionId: 읍/면/동 단위 지역만 설정할 수 있으며, 다른 단위를 보내면 400으로 응답합니다. 해당 지역 이름이 회원의 복지관으로 함께 저장됩니다.
                - latitude / longitude: 도로명 주소 좌표 조회 API의 응답 값을 그대로 전달합니다.
                - detailAddress: 비워서 보내면 null로 저장됩니다.
                - phoneNumber / phoneVerificationToken: 전화번호를 변경할 때만 함께 보냅니다. 변경하지 않으면 둘 다 보내지 않습니다.
                
                두 값 중 하나만 보내면 400(AUTH400_PHONE_CHANGE_REQUEST)으로 응답합니다.
                인증 토큰은 1회용이며, 유효하지 않거나 발급 후 10분 지나면 400으로 응답합니다.
                다른 회원이 이미 사용 중인 전화번호이면 409로 응답합니다.
                """,
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

    @PutMapping("/users/me/word-size")
    @Operation(
            summary = "글자 크기 수정",
            description = """
                회원이 설정한 글자 크기를 변경합니다.
                - wordSize: MEDIUM 또는 LARGE 중 하나입니다.
                
                변경된 값은 이후 로그인 및 토큰 재발급 응답의 wordSize로도 내려갑니다.
                """,
                security = @SecurityRequirement(name = "JWT TOKEN")
    )
    public ApiResponse<Void> updateWordSize(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MemberReqDTO.UpdateWordSize request
    ) {
        memberService.updateWordSize(memberId, request.wordSize());

        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_WORD_SIZE_UPDATED,
                null
        );
    }

    @PostMapping("/users/me/phone-verification/send")
    @Operation(
            summary = "전화번호 변경 인증번호 발송",
            description = """
                개인정보 수정에서 사용할 새 전화번호로 6자리 인증번호를 SMS 발송합니다. 인증번호는 발송 후 5분간 유효합니다.
                다른 회원이 이미 사용 중인 번호로는 발송할 수 없고 409로 응답합니다. 본인이 현재 쓰는 번호는 허용됩니다.
                같은 번호로는 60초 이내에 다시 발송할 수 없으며, 이 경우 429로 응답합니다.
                SMS 발송 자체가 실패하면 502로 응답합니다.
                """,
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
            description = """
                인증번호를 검증하고, 개인정보 수정에 사용할 인증 토큰을 발급합니다.
                - phoneVerificationToken: 발급 후 10분간 유효하며 한 번만 사용할 수 있습니다.
                
                인증번호가 일치하지 않거나 만료되었으면 400으로 응답합니다.
                5회 연속 실패하면 15분간 잠기고 429로 응답합니다.
                """,
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
            description = """
                입력한 현재 비밀번호가 회원 정보와 일치하는지 확인합니다. 비밀번호 변경 화면으로 넘어가기 전 본인 확인 용도입니다.
                - currentPassword: 6자리 숫자입니다.
                - isVerified: 확인에 성공하면 true입니다. 일치하지 않으면 성공 응답 대신 400으로 내려갑니다.
                
                카카오 로그인 회원은 비밀번호가 없어 400으로 응답합니다.
                현재 비밀번호 확인 / 복구 답변 확인 / 비밀번호 변경의 실패 횟수는 함께 집계되며, 5회 실패하면 15분간 잠기고 429로 응답합니다.
                """,
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
            description = """
                회원가입 시 등록한 비밀번호 찾기 답변이 일치하는지 확인합니다.
                답변은 앞뒤 공백만 제거하고 비교하므로 중간 띄어쓰기까지 동일해야 합니다.
                - isVerified: 확인에 성공하면 true입니다. 일치하지 않으면 성공 응답 대신 400으로 내려갑니다.
                
                카카오 로그인 회원은 400으로 응답합니다.
                실패 횟수는 현재 비밀번호 확인과 함께 집계되며, 5회 실패하면 15분간 잠기고 429로 응답합니다.
                """,
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
            description = """
                현재 비밀번호 또는 복구 답변을 다시 검증한 뒤 새 비밀번호로 변경합니다.
                - verificationMethod: CURRENT_PASSWORD면 currentPassword만, RECOVERY_ANSWER면 recoveryAnswer만 함께 보냅니다.
                - newPassword: 6자리 숫자이며, 현재 비밀번호와 같으면 400으로 응답합니다.
                
                검증 방식과 맞지 않게 두 값을 모두 보내거나 모두 비우면 400으로 응답합니다.
                카카오 로그인 회원은 400으로 응답합니다.
                검증에 5회 실패하면 15분간 잠기고 429로 응답합니다.
                변경이 완료되면 리프레시 토큰이 삭제되고 기존 액세스 토큰도 무효가 되므로 다시 로그인해야 합니다.
                """,
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
