package salpim.umc10thsalpim.domain.benefit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/benefits")
@Tag(name = "혜택", description = "복지 혜택 조회 및 신청 지원 API")
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/{benefitId}/application-helper")
    @Operation(
            summary = "신청 도우미 조회",
            description = """
                복지 혜택의 신청 방식, 온라인 신청 가능 여부, 신청 기간과 URL을 조회합니다.
                회원의 지역 및 연령 조건 충족 여부를 함께 제공합니다.
                연령 조건을 확인할 수 없는 경우 isAgeSatisfied는 null로 반환됩니다.
                """
    )
    public ApiResponse<BenefitResDTO.GetApplicationHelperInfo> getApplicationHelperInfoApiResponse(
            @Parameter(description = "조회할 복지 혜택 ID", example = "1")
            @PathVariable Long benefitId
            ){
        BaseSuccessCode code = BenefitSuccessCode.BENEFIT_VIEW;

        Long memberId = 1L; //TODO : get memberId from accessToken

        BenefitResDTO.GetApplicationHelperInfo response =
                benefitService.getApplicationHelperInfo(memberId, benefitId);
        return ApiResponse.onSuccess(code, response);

    }
}
