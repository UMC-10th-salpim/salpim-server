package salpim.umc10thsalpim.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
