package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;

import java.util.List;

public interface BenefitRuleRepository extends JpaRepository<BenefitRule, Long> {

    List<BenefitRule> findAllByWelfareBenefitId(Long welfareBenefitId);
}
