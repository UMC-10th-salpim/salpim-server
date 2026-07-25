package salpim.umc10thsalpim.domain.recommendation.converter;

import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.entity.RecommendationOption;

import java.util.List;

public class RecommendationConverter {
    public static RecommendationResDTO.recommendationOptionsDTO toRecommedationOptionRes(List<RecommendationOption> recommendationOptions) {
        return RecommendationResDTO.recommendationOptionsDTO.builder()
                .recommendationOptionDTOList(
                        recommendationOptions.stream()
                                        .map(
                                                option ->
                                                        RecommendationResDTO.recommendationOptionDTO.builder()
                                                                .optionId(option.getId())
                                                                .optionOrder(option.getOptionOrder())
                                                                .optionText(option.getOptionText())
                                                                .build()
                                        )
                                .toList()
                )
                .build();
    }
}
