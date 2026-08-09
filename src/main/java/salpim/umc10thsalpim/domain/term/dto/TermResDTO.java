package salpim.umc10thsalpim.domain.term.dto;

import lombok.Builder;
import salpim.umc10thsalpim.domain.term.enums.TermsTypeCode;

import java.time.LocalDate;
import java.util.List;

public class TermResDTO {

    // 회원가입 약관 동의 화면 - 약관 목록(종류별 현재 게시 버전)
    @Builder
    public record TermsSummary(
            Long termsTypeId,
            TermsTypeCode code,
            String name,
            Boolean isRequired,
            Integer displayOrder,
            Long termsVersionId,
            String version,
            LocalDate effectiveDate
    ) {}

    // 조항 상세 클릭 - 약관 버전의 조항(제N조) 전문
    @Builder
    public record TermsDetail(
            Long termsTypeId,
            TermsTypeCode code,
            String name,
            Long termsVersionId,
            String version,
            List<ClauseDetail> clauses
    ) {}

    @Builder
    public record ClauseDetail(
            Integer clauseNo,
            String title,
            String content,
            Integer displayOrder
    ) {}

    // 다음 클릭 - 동의 제출 결과
    @Builder
    public record AgreedTerms(
            Long termsTypeId,
            TermsTypeCode code,
            Long termsVersionId,
            Boolean agreed
    ) {}
}
