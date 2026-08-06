package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.benefit.entity.FavoriteBenefit;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;

public interface FavoriteBenefitRepository extends JpaRepository<FavoriteBenefit, Long> {

    @Query(
            value = """
SELECT wb FROM FavoriteBenefit fb JOIN WelfareBenefit wb ON wb.id = fb.benefitId
WHERE fb.memberId = :memberId
ORDER BY fb.id DESC
""",
            countQuery = """
SELECT COUNT(fb) FROM FavoriteBenefit fb
JOIN WelfareBenefit wb ON wb.id = fb.benefitId
WHERE fb.memberId = :memberId
"""
    )
    Page<WelfareBenefit> findFavoriteBenefitsByMemberId(@Param("memberId")Long memberId, Pageable Pageable);

    boolean existsByMemberIdAndBenefitId(Long memberId, Long benefitId);

    void deleteByMemberIdAndBenefitId(Long memberId, Long benefitId);
}
