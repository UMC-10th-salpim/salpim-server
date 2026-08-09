package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.util.List;

public interface WelfareBenefitRepository extends JpaRepository<WelfareBenefit, Long> {

    List<WelfareBenefit> findByExternalIdInAndSource(List<String> externalIdList, String source);


    @Query("SELECT w FROM WelfareBenefit w " +
            "WHERE (w.source = 'NATIONAL' OR (w.source = 'LOCAL' AND w.regionId IN :regionIds)) " +
            "AND EXISTS (SELECT 1 FROM BenefitRule br WHERE br.welfareBenefitId = w.id AND br.applicationType = :applicationType) " +
            "AND w.id > :cursorId " +
            "ORDER BY w.id ASC")
    List<WelfareBenefit> findWelfareBenefitsByRegionAndCursorAndAppType(
            @Param("regionIds") List<Long> regionIds,
            @Param("applicationType")ApplicationType applicationType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
            );
}
