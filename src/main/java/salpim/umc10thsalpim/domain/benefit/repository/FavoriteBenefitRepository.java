package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.benefit.entity.FavoriteBenefit;

public interface FavoriteBenefitRepository extends JpaRepository<FavoriteBenefit, Long> {
    @Query(
            "SELECT fb.benefitId FROM FavoriteBenefit fb WHERE fb.memberId = :memberId"
    )
    Page<Long> findBenefitIdsByMemberId(@Param("memberId") Long memberId, Pageable pageRequest);
}
