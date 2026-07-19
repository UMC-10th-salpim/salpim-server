package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.LocalWelfareBenefit;

import java.util.Optional;

public interface LocalWelfareBenefitRepository extends JpaRepository<LocalWelfareBenefit, Long> {

   Optional<LocalWelfareBenefit> findByServId(String servId);
}
