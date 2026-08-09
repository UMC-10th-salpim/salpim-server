package salpim.umc10thsalpim.domain.term.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.term.converter.TermConverter;
import salpim.umc10thsalpim.domain.term.dto.TermReqDTO;
import salpim.umc10thsalpim.domain.term.dto.TermResDTO;
import salpim.umc10thsalpim.domain.term.entity.MemberAgreement;
import salpim.umc10thsalpim.domain.term.entity.TermsClause;
import salpim.umc10thsalpim.domain.term.entity.TermsType;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;
import salpim.umc10thsalpim.domain.term.enums.TermsVersionStatus;
import salpim.umc10thsalpim.domain.term.exception.AgreementException;
import salpim.umc10thsalpim.domain.term.exception.code.AgreementErrorCode;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsClauseRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsTypeRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsVersionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermsTypeRepository termsTypeRepository;
    private final TermsVersionRepository termsVersionRepository;
    private final TermsClauseRepository termsClauseRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final MemberRepository memberRepository;

    // 회원가입 약관 동의 화면 - 약관 종류별 현재 게시(PUBLISHED) 버전 목록 조회
    public List<TermResDTO.TermsSummary> getSignupTerms() {
        return termsTypeRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(type -> TermConverter.toTermsSummary(type, getPublishedVersionOrThrow(type)))
                .toList();
    }

    // 조항 상세 클릭 - 약관 버전의 조항(제N조) 전문 조회
    public TermResDTO.TermsDetail getTermsDetail(Long termsVersionId) {
        TermsVersion version = termsVersionRepository.findById(termsVersionId)
                .orElseThrow(() -> new AgreementException(AgreementErrorCode.TERM_NOT_FOUND));

        List<TermsClause> clauses = termsClauseRepository.findAllByTermsVersionOrderByDisplayOrderAsc(version);

        return TermConverter.toTermsDetail(version, clauses);
    }

    // 다음 클릭 - 약관 동의 제출
    @Transactional
    public List<TermResDTO.AgreedTerms> submitAgreements(Long memberId, TermReqDTO.SubmitAgreements request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Map<Long, Boolean> agreedByVersionId = request.agreements().stream()
                .collect(Collectors.toMap(
                        TermReqDTO.AgreementItem::termsVersionId,
                        TermReqDTO.AgreementItem::agreed));

        List<TermsVersion> versions = termsVersionRepository.findAllById(agreedByVersionId.keySet());
        if (versions.size() != agreedByVersionId.size()) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }

        validateRequiredTermsAgreed(agreedByVersionId);

        List<MemberAgreement> saved = versions.stream()
                .map(version -> memberTermAgreementRepository.save(
                        TermConverter.toMemberAgreement(member, version, agreedByVersionId.get(version.getId()))))
                .toList();

        return saved.stream().map(TermConverter::toAgreedTerms).toList();
    }

    private void validateRequiredTermsAgreed(Map<Long, Boolean> agreedByVersionId) {
        List<TermsType> requiredTypes = termsTypeRepository.findAllByIsRequiredTrue();

        for (TermsType type : requiredTypes) {
            TermsVersion published = getPublishedVersionOrThrow(type);
            Boolean agreed = agreedByVersionId.get(published.getId());

            if (agreed == null || !agreed) {
                throw new AgreementException(AgreementErrorCode.REQUIRED_TERM_NOT_AGREED);
            }
        }
    }

    private TermsVersion getPublishedVersionOrThrow(TermsType type) {
        return termsVersionRepository.findByTermsTypeAndStatus(type, TermsVersionStatus.PUBLISHED)
                .orElseThrow(() -> new AgreementException(AgreementErrorCode.TERM_NOT_FOUND));
    }
}
