package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;

public interface BenefitRuleRepository extends JpaRepository<BenefitRule, Long> {
}
