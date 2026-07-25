package salpim.umc10thsalpim.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.recommendation.entity.RecommendationOption;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<RecommendationOption, Long> {
    List<RecommendationOption> findAllByCategoryIdOrderByOptionOrderAsc(Long categoryId);
}
