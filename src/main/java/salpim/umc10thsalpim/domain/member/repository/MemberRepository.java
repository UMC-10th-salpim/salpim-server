package salpim.umc10thsalpim.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Member> findByPhoneNumber(String phoneNumber);

    boolean existsByLoginTypeAndKakaoId(SocialProvider loginType, String kakaoId);

    Optional<Member> findByLoginTypeAndKakaoId(SocialProvider loginType, String kakaoId);
}
