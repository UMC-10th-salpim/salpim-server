package salpim.umc10thsalpim.domain.term.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.term.dto.TermReqDTO;
import salpim.umc10thsalpim.domain.term.dto.TermResDTO;
import salpim.umc10thsalpim.domain.term.exception.code.TermSuccessCode;
import salpim.umc10thsalpim.domain.term.service.TermService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/terms")
@Tag(name = "약관", description = "회원가입 약관 조회 및 동의 API")
public class TermController {

    private final TermService termService;

    @GetMapping
    @Operation(
            summary = "회원가입 약관 목록 조회",
            description = """
                회원가입 약관 동의 화면에서 보여줄 약관 목록을 조회합니다.
                약관 종류(TermsType)마다 현재 게시(PUBLISHED)된 버전 정보를 함께 반환합니다.
                isRequired가 true인 약관은 반드시 동의해야 하는 필수 항목입니다.
                """
    )
    public ApiResponse<List<TermResDTO.TermsSummary>> getSignupTerms() {
        return ApiResponse.onSuccess(TermSuccessCode.TERM_VIEW, termService.getSignupTerms());
    }

    @GetMapping("/{termsVersionId}")
    @Operation(
            summary = "약관 조항 상세 조회",
            description = "약관 목록에서 특정 약관을 클릭했을 때, 해당 약관 버전의 조항(제N조) 전문을 조회합니다."
    )
    public ApiResponse<TermResDTO.TermsDetail> getTermsDetail(
            @Parameter(description = "조회할 약관 버전 ID", example = "1")
            @PathVariable Long termsVersionId
    ) {
        return ApiResponse.onSuccess(TermSuccessCode.TERM_VIEW, termService.getTermsDetail(termsVersionId));
    }

    @PostMapping("/agreements")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "약관 동의 제출",
            description = """
                회원가입 약관 동의 화면에서 '다음' 클릭 시 호출합니다.
                제출한 약관 버전 각각에 대한 동의 여부를 이력으로 저장합니다.
                필수 약관(isRequired=true)에 agreed=true로 동의하지 않으면 REQUIRED_TERM_NOT_AGREED(400) 에러가 발생합니다.
                """
    )
    public ResponseEntity<ApiResponse<List<TermResDTO.AgreedTerms>>> submitAgreements(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody TermReqDTO.SubmitAgreements request
    ) {
        List<TermResDTO.AgreedTerms> response = termService.submitAgreements(memberId, request);
        return ResponseEntity.status(TermSuccessCode.TERM_AGREEMENT_SUBMITTED.getStatus())
                .body(ApiResponse.onSuccess(TermSuccessCode.TERM_AGREEMENT_SUBMITTED, response));
    }
}
