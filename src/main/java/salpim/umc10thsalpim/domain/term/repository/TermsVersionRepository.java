package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.term.entity.TermsType;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;
import salpim.umc10thsalpim.domain.term.enums.TermsVersionStatus;

import java.util.Optional;

public interface TermsVersionRepository extends JpaRepository<TermsVersion, Long> {

    Optional<TermsVersion> findByTermsTypeAndStatus(TermsType termsType, TermsVersionStatus status);
}
