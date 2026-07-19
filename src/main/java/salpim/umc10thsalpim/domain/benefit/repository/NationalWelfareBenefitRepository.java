package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.NationalWelfareBenefit;

import java.util.Optional;

public interface NationalWelfareBenefitRepository extends JpaRepository<NationalWelfareBenefit, Long> {

   Optional<NationalWelfareBenefit> findByServId(String servId);
}
