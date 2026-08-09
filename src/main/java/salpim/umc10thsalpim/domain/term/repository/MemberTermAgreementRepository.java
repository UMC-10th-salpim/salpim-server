package salpim.umc10thsalpim.domain.term.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.term.entity.MemberAgreement;

import java.util.List;

public interface MemberTermAgreementRepository extends JpaRepository<MemberAgreement, Long> {

    void deleteByMember(Member member);

    List<MemberAgreement> findAllByMember(Member member);
}
