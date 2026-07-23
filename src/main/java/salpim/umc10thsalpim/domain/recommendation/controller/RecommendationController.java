package salpim.umc10thsalpim.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.exception.code.RecommendationSuccessCode;
import salpim.umc10thsalpim.domain.recommendation.service.RecommendationService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;

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
                2단계 선택지들의 id와 선택지 내용, 순서를 가진 리스트를 보낸다
                """
    )
    public ApiResponse<RecommendationResDTO.recommendationOptionsDTO> getRecommendationOptions(
            @PathVariable Long categoryId
    ){
        return ApiResponse.onSuccess(RecommendationSuccessCode.OPTION_LIST_GET_SUCCESS, recommendationService.getRecommendationOptions(categoryId));
    }

}
