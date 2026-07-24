package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;

import java.util.List;

public interface WelfareBenefitRepository extends JpaRepository<WelfareBenefit, Long> {

    List<WelfareBenefit> findByExternalIdInAndSource(List<String> externalIdList, String source);
}
