package salpim.umc10thsalpim.domain.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.auth.entity.PasswordVerificationAttempt;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;

import java.util.Optional;

public interface PasswordVerificationAttemptRepository extends JpaRepository<PasswordVerificationAttempt, Long> {

    Optional<PasswordVerificationAttempt> findByPurposeAndTargetTypeAndTargetValue(
            PasswordVerificationPurpose purpose,
            PasswordVerificationTargetType targetType,
            String targetValue
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select attempt
            from PasswordVerificationAttempt attempt
            where attempt.purpose = :purpose
                and attempt.targetType = :targetType
                and attempt.targetValue = :targetValue
            """)
    Optional<PasswordVerificationAttempt> findByPurposeAndTargetTypeAndTargetValueForUpdate(
            @Param("purpose") PasswordVerificationPurpose purpose,
            @Param("targetType") PasswordVerificationTargetType targetType,
            @Param("targetValue") String targetValue
    );
}
