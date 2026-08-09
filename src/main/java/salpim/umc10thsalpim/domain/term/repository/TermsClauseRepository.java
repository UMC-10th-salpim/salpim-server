package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.term.entity.TermsClause;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;

import java.util.List;

public interface TermsClauseRepository extends JpaRepository<TermsClause, Long> {

    List<TermsClause> findAllByTermsVersionOrderByDisplayOrderAsc(TermsVersion termsVersion);
}
