package salpim.umc10thsalpim.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.exception.code.RecommendationSuccessCode;
import salpim.umc10thsalpim.domain.recommendation.service.RecommendationService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "추천", description = "살피미 혜택 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/options/{categoryId}")
    @Operation(
            summary = "2단계 질문 리스트 조회",
            description = """
                1단계에서 선택한 카테고리에 속한 2단계 선택지 목록을 조회합니다.
                - optionId: 살피미 추천 결과 조회 API에 들어갈 optionId로 그대로 전달합니다.
                - optionText: 화면에 노출할 선택지 문구입니다.
                - optionOrder: 선택지 노출 순서입니다.
                - searchWrds: 해당 선택지가 추천 결과 조회 시 사용하는 복지로 검색어
                """
    )
    public ApiResponse<RecommendationResDTO.RecommendationOptionsDTO> getRecommendationOptions(
            @Parameter(description = "1단계에서 선택한 선택지의 카테고리 ID")
            @PathVariable Long categoryId
    ){
        return ApiResponse.onSuccess(RecommendationSuccessCode.OPTION_LIST_GET_SUCCESS, recommendationService.getRecommendationOptions(categoryId));
    }

    @GetMapping("/result")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(
            summary = "살피미 추천 결과",
            description = """
                2단계에서 선택한 선택지를 기반으로 추천 복지 혜택 목록을 커서 기반 페이지네이션으로 조회합니다.
                선택지에 등록된 검색어와 카테고리, 그리고 로그인한 회원의 지역으로 복지로를 검색하므로 지역을 따로 보낼 필요가 없습니다.
                정렬은 복지로 조회수순으로 고정
                복지로 API를 실시간 호출하므로 응답까지 수 초가 걸릴 수 있습니다.
                
                - 첫 요청에는 cursor는 -1입니다.
                - 이후 요청에는 이전 응답의 nextCursor 값을 cursor로 넣어 다음 페이지를 조회합니다.
                - totalCount: 카테고리 필터까지 적용된 전체 검색 결과 수
                
                존재하지 않는 optionId를 보내거나 회원의 지역 정보를 찾을 수 없으면 404로 응답합니다.
                """
    )
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getRecommendationResult(
            @Parameter(description = "2단계에서 선택한 선택지 ID")
            @RequestParam(name = "optionId") Long  lastOptionId,
            @Parameter(description = "직전 응답의 nextCursor. 첫 페이지는 -1")
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @Parameter(description = "한 페이지에 반환할 개수")
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize,
            @AuthenticationPrincipal Long memberId
    ){
        return ApiResponse.onSuccess(RecommendationSuccessCode.RECOMMENDATION_GET_SUCCESS, recommendationService.getRecommendationResult(lastOptionId, memberId, cursor, pageSize));
    }

}
