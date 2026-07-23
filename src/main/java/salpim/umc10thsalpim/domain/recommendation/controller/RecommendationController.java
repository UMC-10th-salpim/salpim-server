package salpim.umc10thsalpim.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.exception.code.RecommendationSuccessCode;
import salpim.umc10thsalpim.domain.recommendation.service.RecommendationService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "추천", description = "복지 혜택 살피미 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/options/{categoryId}")
    @Operation(
            summary = "2단계 질문 리스트 조회",
            description = """
                선택한 1단계 선택지의 카테고리를를 path variable로 주면
                2단계 선택지들의 id와 선택지 내용, 순서를 가진 리스트를 보낸다.
                """
    )
    public ApiResponse<RecommendationResDTO.recommendationOptionsDTO> getRecommendationOptions(
            @PathVariable Long categoryId
    ){
        return ApiResponse.onSuccess(RecommendationSuccessCode.OPTION_LIST_GET_SUCCESS, recommendationService.getRecommendationOptions(categoryId));
    }

    @GetMapping("/result")
    @Operation(
            summary = "살피미 추천 결과",
            description = """
                2단계에서 선택한 선택지(lastOptionId)를 기반으로 추천 복지 혜택 목록을 커서 기반 페이지네이션으로 조회합니다.
                - 첫 요청에는 cursor는 -1입니다.
                - 이후 요청에는 이전 응답의 nextCursor 값을 cursor로 넣어 다음 페이지를 조회합니다.
                - hasNext가 false이면 마지막 페이지입니다.
                """
    )
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getRecommendationResult(
            @RequestParam(name = "optionId") Long  optionId,
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize
    ){

        Long memberId = 1L; //TODO : get memberId from accessToken

        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, recommendationService.getRecommendationResult(optionId, memberId, cursor, pageSize));
    }

}
