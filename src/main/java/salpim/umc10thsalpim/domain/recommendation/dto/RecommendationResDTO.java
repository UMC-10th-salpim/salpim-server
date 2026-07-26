package salpim.umc10thsalpim.domain.recommendation.dto;

import lombok.Builder;

import java.util.List;

public class RecommendationResDTO {

    @Builder
    public record RecommendationOptionDTO(
      Long optionId,
      Integer optionOrder,
      String optionText
    ){}

    @Builder
    public record RecommendationOptionsDTO(
            List<RecommendationOptionDTO> recommendationOptionDTOList
    ){}

}
