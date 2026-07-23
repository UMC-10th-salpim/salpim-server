package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.term.entity.MemberTermAgreement;

public interface MemberTermAgreementRepository extends JpaRepository<MemberTermAgreement, Long> {

    void deleteByMember(Member member);
}
