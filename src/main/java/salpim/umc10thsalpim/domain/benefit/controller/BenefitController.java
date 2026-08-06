package salpim.umc10thsalpim.domain.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitReqDTO;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.net.URI;
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
            @RequestParam @Size(min = 2, max = 2) List<Long> regionIds, //필수
            @RequestParam(required = false) List<Long> categoryIds, //null -> 모든 카테고리
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize,
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ) {
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }



    @GetMapping("/{benefitId}/application-helper")
    @SecurityRequirement(name = "JWT TOKEN")
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
            @PathVariable Long benefitId,
            @AuthenticationPrincipal Long memberId
            ){
        BaseSuccessCode code = BenefitSuccessCode.BENEFIT_VIEW;

        BenefitResDTO.GetApplicationHelperInfo response =
                benefitService.getApplicationHelperInfo(memberId, benefitId);
        return ApiResponse.onSuccess(code, response);

    }

    @GetMapping("/{benefitId}/application-link")
    @Operation(
            summary = "온라인 신청 사이트로 이동",
            description = "온라인 신청이 가능한 복지 혜택의 신청 사이트로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "온라인 신청 사이트 리다이렉트 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "온라인 신청을 지원하지 않는 혜택"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "복지 혜택 또는 신청 규칙을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "신청 URL이 없거나 형식이 올바르지 않음"
            )
    })
    public ResponseEntity<Void> redirectToOnlineApplication(
            @Parameter(description = "온라인 신청할 복지 혜택 ID", example = "1")
            @PathVariable Long benefitId
    ) {
        String applicationUrl = benefitService.getOnlineApplicationUrl(benefitId);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(applicationUrl))
                .build();
    }

    @GetMapping("/favorites")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "찜한 혜택 조회",
            description = """
                로그인한 회원이 찜한 복지 혜택 목록을 페이지 단위로 조회합니다.
                - pageNumber: 조회할 페이지 번호 0부터 시작하며 기본값은 0
                - pageSize: 한 페이지에 반환할 개수
                - totalCount는 회원이 찜한 전체 혜택 개수
                - pageSize는 현재 페이지에 실제로 담긴 개수
                - hasNext가 false이면 마지막 페이지이다.
                - applicationEndDate와 minAge는 혜택에 해당 정보가 없으면 null로 반환됩니다.
                """
    )
   public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.FavoriteBenefitDTO>> getFavoriteBenefits(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(name="pageNumber", defaultValue = "0") @PositiveOrZero Integer pageNumber,
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize
    ){
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getFavoriteBenefits(memberId, pageNumber, pageSize));
    }

    @PutMapping("/{benefitId}/favorite")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "혜택 찜하기/찜 취소",
            description = """
                혜택 찜 상태를 요청한 상태로 변경합니다.
                - benefitId: 찜 상태 변경을 원하는 혜택 id를 path로 주기
                - updateFavorite: 어떤 상태로 변하길 원하는 지를 body로 주기
                
                이미 원하는 상태인 경우에도 성공으로 응답합니다.
                """
    )
    public ApiResponse<BenefitResDTO.FavoriteBenefitStatusDTO> updateFavoriteBenefit(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long benefitId,
            @Valid @RequestBody BenefitReqDTO.UpdateFavorite req
    ){
        return ApiResponse.onSuccess(req.isFavorite() ? BenefitSuccessCode.BENEFIT_FAVORITE_ADD : BenefitSuccessCode.BENEFIT_FAVORITE_REMOVE,
                benefitService.toggleFavoriteBenefit(memberId, benefitId, req.isFavorite()));
    }

}
