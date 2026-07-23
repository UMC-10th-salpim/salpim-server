package salpim.umc10thsalpim.domain.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/benefits")
@Tag(name = "혜택", description = "복지 혜택 조회 및 신청 지원 API")
public class BenefitController {

    private final BenefitService benefitService;

    @GetMapping("/search")
    @Operation(
            summary = "혜택 안내/직접 찾기",
            description = """
                검색어와 지역 기반으로 복지로(중앙부처/지자체) 복지 혜택을 검색합니다.
                복지로 검색 결과 중 서비스 DB에 등록된 혜택만 반환됩니다.
                - searchKey: 검색어. 생략 시 전체 조회됩니다.
                - regionIds: 지역 ID 2개를 [시/도, 시/군/구] 순서로 전달합니다. 지자체 혜택 검색에 사용됩니다. (필수)
                - categoryIds: 카테고리 ID 목록. 생략 시 모든 카테고리가 조회됩니다.
                - sort: popular(기본값) - 복지로 조회수 기준 인기순 / deadline - 신청 마감일 임박순, 마감일이 없는 혜택은 뒤로 정렬됩니다. 그 외 값은 INVALID_SORT_TYPE 에러가 발생합니다.
                - cursor: 첫 페이지는 -1(기본값), 다음 페이지부터는 직전 응답의 nextCursor 값을 그대로 전달합니다.
                - pageSize: 한 페이지에 반환할 개수. 기본값 10입니다.
                nextCursor가 null이면 마지막 페이지입니다.
                totalCount는 카테고리 필터까지 적용된 전체 검색 결과 수입니다.
                """
    )
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getSearchResult
    (
            @RequestParam(required = false, defaultValue = "")
            String searchKey,
            @RequestParam List<Long> regionIds, //필수
            @RequestParam(required = false) List<Long> categoryIds, //null -> 모든 카테고리
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize,
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ) {
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }



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
