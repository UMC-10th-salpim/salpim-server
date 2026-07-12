package salpim.umc10thsalpim.domain.benefit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/benefits")
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/{benefitId}/application-helper")
    public ApiResponse<BenefitResDTO.GetApplicationHelperInfo> getApplicationHelperInfoApiResponse(
            @PathVariable Long benefitId
            ){
        BaseSuccessCode code = BenefitSuccessCode.BENEFIT_VIEW;
        BenefitResDTO.GetApplicationHelperInfo response =
                benefitService.getApplicationHelperInfo(benefitId);
        return ApiResponse.onSuccess(code, response);

    }
}
