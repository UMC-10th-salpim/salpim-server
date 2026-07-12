package salpim.umc10thsalpim.domain.term.converter;

import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.term.entity.MemberTermAgreement;
import salpim.umc10thsalpim.domain.term.entity.Term;

import java.time.LocalDateTime;

public final class AgreementConverter {

    private AgreementConverter() {
    }

    public static MemberTermAgreement toMemberTermAgreement(Member member, Term term) {
        return MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .agreedAt(LocalDateTime.now())
                .build();
    }
}
