package salpim.umc10thsalpim.domain.welfare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.welfare.dto.WelfareResDTO;
import salpim.umc10thsalpim.domain.welfare.exception.code.WelfareSuccessCode;
import salpim.umc10thsalpim.domain.welfare.service.WelfareService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api")
public class WelfareController {

    private final WelfareService welfareService;

    //직접 찾기 검색 api
    @GetMapping("/v1/welfare-benefits/search")
    public ApiResponse<CursorResDTO.Pagination<WelfareResDTO.WelfareSearchResultDTO>> getSearchResult
    (
            @RequestParam String searchKey,
            @RequestParam List<Integer> regionIds,
            @RequestParam List<Integer> categoryIds,
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ){
        return ApiResponse.onSuccess(WelfareSuccessCode.BENEFIT_LIST_GET_SUCCESS, welfareService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }
}
