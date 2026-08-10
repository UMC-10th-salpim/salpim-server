package salpim.umc10thsalpim.domain.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.member.entity.Member;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMember(Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select refreshToken
            from RefreshToken refreshToken
            join fetch refreshToken.member
            where refreshToken.member.id = :memberId
            """)
    Optional<RefreshToken> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    void deleteByMember(Member member);
}
