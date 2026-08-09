package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.term.entity.TermsType;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;
import salpim.umc10thsalpim.domain.term.enums.TermsVersionStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TermsVersionRepository extends JpaRepository<TermsVersion, Long> {

    Optional<TermsVersion> findByTermsTypeAndStatus(TermsType termsType, TermsVersionStatus status);

    List<TermsVersion> findAllByTermsTypeInAndStatus(Collection<TermsType> termsTypes, TermsVersionStatus status);

    @Query("SELECT v FROM TermsVersion v JOIN FETCH v.termsType WHERE v.id IN :ids")
    List<TermsVersion> findAllByIdInFetchTermsType(@Param("ids") Collection<Long> ids);
}
