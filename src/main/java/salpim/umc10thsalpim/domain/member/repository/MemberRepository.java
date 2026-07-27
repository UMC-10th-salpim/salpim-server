package salpim.umc10thsalpim.domain.member.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long memberId);

    Optional<Member> findByPhoneNumber(String phoneNumber);

    boolean existsByLoginTypeAndKakaoId(SocialProvider loginType, String kakaoId);

    Optional<Member> findByLoginTypeAndKakaoId(SocialProvider loginType, String kakaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select member from Member member where member.id = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);
}
