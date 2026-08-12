package salpim.umc10thsalpim.domain.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.auth.entity.TermsAgreementVerification;

import java.util.Optional;

public interface TermsAgreementVerificationRepository extends JpaRepository<TermsAgreementVerification, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select verification
            from TermsAgreementVerification verification
            where verification.phoneNumber = :phoneNumber
            """)
    Optional<TermsAgreementVerification> findByPhoneNumberForUpdate(@Param("phoneNumber") String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}
