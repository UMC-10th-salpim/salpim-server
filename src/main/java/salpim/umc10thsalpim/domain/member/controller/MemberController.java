package salpim.umc10thsalpim.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.member.service.MemberWithdrawalService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberWithdrawalService memberWithdrawalService;

    @Operation(
            summary = "회원 탈퇴 API",
            description = "Bearer Access Token으로 인증된 현재 회원과 회원 소유 인증·약관 동의 데이터를 삭제합니다. 탈퇴 후 기존 토큰은 사용할 수 없습니다.",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long memberId
    ) {
        memberWithdrawalService.withdraw(memberId);
        return ResponseEntity.status(GeneralSuccessCode.DELETED.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.DELETED, null));
    }
}
