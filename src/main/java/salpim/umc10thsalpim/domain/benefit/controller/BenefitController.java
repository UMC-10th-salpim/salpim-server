package salpim.umc10thsalpim.domain.benefit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.global.apiPayload.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
public class BenefitController {

    private final BenefitService benefitService;

    //직접 찾기 검색 api
    @GetMapping("/v1/welfare-benefits/search")
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getSearchResult
    (
            @RequestParam String searchKey,
            @RequestParam List<Long> regionIds,
            @RequestParam List<Long> categoryIds,
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ){
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_LIST_GET_SUCCESS, benefitService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }
}
