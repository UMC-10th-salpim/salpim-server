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
            description = "회원 이름과 시도, 시군구 정보를 조회합니다."
    )
    public ApiResponse<MemberResDTO.MyPageInfo> getMyPage(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.MEMBER_MY_PAGE_VIEW,
                memberService.getMyPage(memberId)
        );
    }

    @PutMapping("/users/me")
    @Operation(
            summary = "개인정보 수정",
            description = "회원의 이름, 생년월일, 성별, 주소, 거주지역 정보를 수정합니다."
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
}
