package salpim.umc10thsalpim.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import salpim.umc10thsalpim.domain.recommendation.converter.RecommendationConverter;
import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.entity.RecommendationOption;
import salpim.umc10thsalpim.domain.recommendation.repository.RecommendationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationResDTO.recommendationOptionsDTO getRecommendationOptions(Long categoryId) {
        List<RecommendationOption>  recommendationOptions = recommendationRepository.findAllByCategoryId(categoryId);
        return RecommendationConverter.toRecommedationOptionRes(recommendationOptions);
    }
}
