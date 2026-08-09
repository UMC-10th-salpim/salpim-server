package salpim.umc10thsalpim.domain.term.converter;

import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.term.dto.TermResDTO;
import salpim.umc10thsalpim.domain.term.entity.MemberAgreement;
import salpim.umc10thsalpim.domain.term.entity.TermsClause;
import salpim.umc10thsalpim.domain.term.entity.TermsType;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;

import java.util.List;

public class TermConverter {

    public static TermResDTO.TermsSummary toTermsSummary(TermsType type, TermsVersion version) {
        return TermResDTO.TermsSummary.builder()
                .termsTypeId(type.getId())
                .code(type.getCode())
                .name(type.getName())
                .isRequired(type.getIsRequired())
                .displayOrder(type.getDisplayOrder())
                .termsVersionId(version.getId())
                .version(version.getVersion())
                .effectiveDate(version.getEffectiveDate())
                .build();
    }

    public static TermResDTO.TermsDetail toTermsDetail(TermsVersion version, List<TermsClause> clauses) {
        TermsType type = version.getTermsType();

        return TermResDTO.TermsDetail.builder()
                .termsTypeId(type.getId())
                .code(type.getCode())
                .name(type.getName())
                .termsVersionId(version.getId())
                .version(version.getVersion())
                .clauses(clauses.stream().map(TermConverter::toClauseDetail).toList())
                .build();
    }

    public static TermResDTO.ClauseDetail toClauseDetail(TermsClause clause) {
        return TermResDTO.ClauseDetail.builder()
                .clauseNo(clause.getClauseNo())
                .title(clause.getTitle())
                .content(clause.getContent())
                .displayOrder(clause.getDisplayOrder())
                .build();
    }

    public static MemberAgreement toMemberAgreement(Member member, TermsVersion version, Boolean agreed) {
        return MemberAgreement.builder()
                .member(member)
                .termsVersion(version)
                .agreed(agreed)
                .build();
    }

    public static TermResDTO.AgreedTerms toAgreedTerms(MemberAgreement agreement) {
        TermsVersion version = agreement.getTermsVersion();
        TermsType type = version.getTermsType();

        return TermResDTO.AgreedTerms.builder()
                .termsTypeId(type.getId())
                .code(type.getCode())
                .termsVersionId(version.getId())
                .agreed(agreement.getAgreed())
                .build();
    }
}
