package salpim.umc10thsalpim.domain.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select passwordResetToken
            from PasswordResetToken passwordResetToken
            where passwordResetToken.tokenIdHash = :tokenIdHash
            """)
    Optional<PasswordResetToken> findByTokenIdHashForUpdate(
            @Param("tokenIdHash") String tokenIdHash
    );

    void deleteByMemberId(Long memberId);
}
