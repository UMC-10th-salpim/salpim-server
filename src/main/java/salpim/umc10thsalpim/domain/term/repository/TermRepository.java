package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.term.entity.Term;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByRequiredTrue();
}
