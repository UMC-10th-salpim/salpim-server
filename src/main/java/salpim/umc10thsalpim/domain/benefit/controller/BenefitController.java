package salpim.umc10thsalpim.domain.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
            summary = "혜택 직접 조회",
            description = """
                검색어와 지역 기반으로 복지로(중앙부처/지자체) 복지 혜택을 검색합니다.
                복지로 검색 결과 중 서비스 DB에 등록된 혜택만 반환됩니다.
                복지로 API를 실시간 호출하므로 응답까지 수 초가 걸릴 수 있습니다.
                - totalCount: 카테고리 필터까지 적용된 전체 검색 결과 수
                - pageSize: 현재 페이지에 실제로 담긴 개수
                """
    )
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getSearchResult
    (
            @Parameter(description = "검색어. 생략 시 전체 조회")
            @RequestParam(required = false, defaultValue = "")
            String searchKey,
            @Parameter(description = "지역 ID 2개를 [시/도, 시/군/구] 계층으로 전달, 생략 불가")
            @RequestParam @Size(min = 2, max = 2) List<Long> regionIds,
            @Parameter(description = "카테고리 ID 목록. 생략 시 전체 카테고리")
            @RequestParam(required = false) List<Long> categoryIds,
            @Parameter(description = "직전 응답의 nextCursor. 첫 페이지는 -1")
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @Parameter(description = "한 페이지에 반환할 개수")
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize,
            @Parameter(
                    description = "정렬 방식 (popular: 복지로 조회수순 / deadline: 마감 임박순, 마감일 없는 혜택은 뒤로)",
                    schema = @Schema(allowableValues = {"popular", "deadline"}, defaultValue = "popular"))
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ) {
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }



    @GetMapping("/{benefitId}")
    @Operation(
            summary = "혜택 상세 조회",
            description = """
                    복지 혜택의 상세 정보(요약, 자격, 혜택 내용, 신청 기간 등)를 조회합니다.
                    - ageConditionStatus: UNKNOWN이면 연령 조건을 확인할 수 없는 혜택이고, NO_RESTRICTION이면 연령 제한이 없습니다. RESTRICTED인 경우에도 minAge / maxAge 중 한쪽만 있을 수 있습니다.
                    - isOnlineApplicationAvailable: true인 경우에만 온라인 신청 이동 API를 호출할 수 있습니다.
                    """
    )
    public ApiResponse<BenefitResDTO.GetBenefitDetailDTO> getBenefitDetail(
            @Parameter(description = "조회할 복지 혜택 ID", example = "1")
            @PathVariable Long benefitId
    ) {
        BenefitReqDTO.GetBenefitDetailDTO request = new BenefitReqDTO.GetBenefitDetailDTO(benefitId);
        BenefitResDTO.GetBenefitDetailDTO response = benefitService.getBenefitDetail(request);
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, response);
    }

    @GetMapping("/{benefitId}/application-helper")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "신청 도우미 조회",
            description = """
                복지 혜택의 신청 방식, 온라인 신청 가능 여부, 신청 기간과 URL을 조회합니다.
                회원의 지역 및 연령 조건 충족 여부를 함께 제공합니다.
                - isAgeSatisfied: 연령 조건을 확인할 수 없는 경우 null로 반환됩니다.
                - isRegionSatisfied: 혜택의 지역 범위 기준으로 회원 지역의 상위 지역을 거슬러 올라가 비교한 결과이며, 전국 혜택은 항상 true입니다.
                - applicationTypeList: VISIT / ONLINE / PHONE / OTHER 중 중복 제거된 목록입니다.
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
            description = """
                    온라인 신청이 가능한 복지 혜택의 신청 사이트로 302 리다이렉트합니다.
                    응답 본문은 없고 Location 헤더에 신청 URL이 담깁니다.
                    신청 도우미 조회의 isOnlineApplicationAvailable이 true인 혜택에만 호출해야 하며, false인 혜택은 400으로 차단됩니다.
                    """    )
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
                로그인한 회원이 찜한 복지 혜택 목록을 페이지 단위로 조회합니다. 최근에 찜한 순서로 정렬됩니다.                - pageNumber: 조회할 페이지 번호 0부터 시작하며 기본값은 0
                - pageSize: 한 페이지에 반환한 개수
                - totalCount: 회원이 찜한 전체 혜택 개수
                - hasNext: false이면 마지막 페이지이다.
                applicationEndDate와 minAge는 혜택에 해당 정보가 없으면 null로 반환됩니다.
                """
    )
   public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.FavoriteBenefitDTO>> getFavoriteBenefits(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "조회할 페이지 번호 (0부터 시작)")
            @RequestParam(name="pageNumber", defaultValue = "0") @PositiveOrZero Integer pageNumber,
            @Parameter(description = "한 페이지에 반환할 개수")
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize
    ){
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getFavoriteBenefits(memberId, pageNumber, pageSize));
    }

    @PutMapping("/{benefitId}/favorite")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "혜택 찜하기/찜 취소",
            description = """
                혜택 찜 상태를 요청한 상태로 변경합니다. isFavorite에 원하는 상태를 그대로 담아 보냅니다.
                - benefitId: 찜 상태 변경을 원하는 혜택 id를 path로 주기
                - isFavorite: 어떤 상태로 변하길 원하는 지를 body로 주기
                
                이미 원하는 상태인 경우에도 성공으로 응답합니다.
                찜한 경우 BENEFIT200_2, 찜을 해제한 경우 BENEFIT200_3이 반환됩니다.
                """
    )
    public ApiResponse<BenefitResDTO.FavoriteBenefitStatusDTO> updateFavoriteBenefit(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "찜 상태를 변경할 혜택 ID")
            @PathVariable Long benefitId,
            @Valid @RequestBody BenefitReqDTO.UpdateFavorite req
    ){
        return ApiResponse.onSuccess(req.isFavorite() ? BenefitSuccessCode.BENEFIT_FAVORITE_ADD : BenefitSuccessCode.BENEFIT_FAVORITE_REMOVE,
                benefitService.toggleFavoriteBenefit(memberId, benefitId, req.isFavorite()));
    }

    @GetMapping("/favorites/deadline-soon")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "마감일 임박한 혜택 조회",
            description = """
                로그인한 회원이 찜한 혜택 중 마감일이 임박한 순으로 3개를 조회합니다.
                마감일이 이미 지난 혜택은 제외됩니다.
                마감일이 있는 혜택이 먼저 오고, 마감일이 없는 혜택은 뒤에 ID 오름차순으로 옵니다.
                조건에 맞는 혜택이 없으면 빈 배열이 반환됩니다.
                - dDay: 서버 기준(KST) 남은 일수입니다. 0이면 오늘 마감이고, 마감일이 없으면 null입니다.
                """
    )
    public ApiResponse<List<BenefitResDTO.DeadlineSoonBenefitDTO>> getDeadlineSoonBenefits(
            @AuthenticationPrincipal Long memberId
    ){
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getDeadlineSoonBenefits(memberId));
    }

    @Operation(summary = "카카오톡 공유하기용 혜택 조회",
            description = """
                    카카오톡 공유하기 시 보여줄 혜택의 제목과 요약 정보를 조회합니다.
                    summary는 혜택의 쉬운 설명이며, 등록되지 않은 혜택은 null입니다.
                    """
    )
    @GetMapping("/{benefitId}/share")
    public ApiResponse<BenefitResDTO.BenefitShareDTO> getBenefitShareInfo(
            @Parameter(description = "조회할 복지 혜택 ID")
            @PathVariable Long benefitId
    ){
        BenefitResDTO.BenefitShareDTO result = benefitService.getBenefitShareInfo(benefitId);
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_SHARE_SUCCESS, result);
    }
}
