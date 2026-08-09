package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.term.entity.TermsType;

import java.util.List;

public interface TermsTypeRepository extends JpaRepository<TermsType, Long> {

    List<TermsType> findAllByOrderByDisplayOrderAsc();

    List<TermsType> findAllByIsRequiredTrue();
}
