package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;

public interface WelfareBenefitRepository extends JpaRepository<WelfareBenefit, Long> {
}
