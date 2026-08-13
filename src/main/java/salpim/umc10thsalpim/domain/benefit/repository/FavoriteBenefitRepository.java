package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.benefit.entity.FavoriteBenefit;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;

import java.time.LocalDate;
import java.util.List;

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

    long countByMemberIdAndBenefitId(Long memberId, Long benefitId);

    void deleteByMemberIdAndBenefitId(Long memberId, Long benefitId);

    void deleteByMemberId(Long memberId);

    @Query("""
SELECT wb FROM FavoriteBenefit fb JOIN WelfareBenefit wb ON wb.id = fb.benefitId
WHERE fb.memberId = :memberId
  AND (wb.applicationEndDate IS NULL OR wb.applicationEndDate >= :today)
ORDER BY CASE WHEN wb.applicationEndDate IS NULL THEN 1 ELSE 0 END ASC,
         wb.applicationEndDate ASC,
         wb.id ASC
""")
    List<WelfareBenefit> findDeadlineSoonFavoriteBenefits(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Modifying
    @Query(value = """
INSERT IGNORE INTO favorite_benefit (member_id, benefit_id, created_at, updated_at)
VALUES (:memberId, :benefitId, NOW(), NOW())
""", nativeQuery = true)
    void insertIgnore(@Param("memberId") Long memberId, @Param("benefitId") Long benefitId);

}
