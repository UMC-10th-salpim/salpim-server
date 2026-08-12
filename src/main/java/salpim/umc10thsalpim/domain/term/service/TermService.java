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
        List<TermsType> types = termsTypeRepository.findAllByOrderByDisplayOrderAsc();
        Map<Long, TermsVersion> publishedByTypeId = getPublishedVersionsByType(types);

        return types.stream()
                .map(type -> TermConverter.toTermsSummary(type, getPublishedVersionOrThrow(type, publishedByTypeId)))
                .toList();
    }

    // 조항 상세 클릭 - 약관 버전의 조항(제N조) 전문 조회
    public TermResDTO.TermsDetail getTermsDetail(Long termsVersionId) {
        TermsVersion version = termsVersionRepository.findById(termsVersionId)
                .orElseThrow(() -> new AgreementException(AgreementErrorCode.TERM_NOT_FOUND));
        if (!version.isPublished()) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }

        List<TermsClause> clauses = termsClauseRepository.findAllByTermsVersionOrderByDisplayOrderAsc(version);

        return TermConverter.toTermsDetail(version, clauses);
    }

    // 다음 클릭 - 약관 동의 제출
    @Transactional
    public List<TermResDTO.AgreedTerms> submitAgreements(Long memberId, TermReqDTO.SubmitAgreements request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Map<TermsVersion, Boolean> resolvedAgreements = resolveAndValidateAgreements(request.agreements());

        List<MemberAgreement> saved = resolvedAgreements.entrySet().stream()
                .map(entry -> memberTermAgreementRepository.save(
                        TermConverter.toMemberAgreement(member, entry.getKey(), entry.getValue())))
                .toList();

        return saved.stream().map(TermConverter::toAgreedTerms).toList();
    }

    // 약관 버전 ID 목록을 실제 게시된 TermsVersion으로 해석하고, 필수 약관 동의 여부를 검증한다.
    // 회원가입 전 약관 동의 사전 검증(TermsAgreementVerificationService)에서도 재사용한다.
    public Map<TermsVersion, Boolean> resolveAndValidateAgreements(List<TermReqDTO.AgreementItem> agreements) {
        Map<Long, Boolean> agreedByVersionId = agreements.stream()
                .collect(Collectors.toMap(
                        TermReqDTO.AgreementItem::termsVersionId,
                        TermReqDTO.AgreementItem::agreed));

        List<TermsVersion> versions = termsVersionRepository.findAllByIdInFetchTermsType(agreedByVersionId.keySet());
        if (versions.size() != agreedByVersionId.size()) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }
        if (versions.stream().anyMatch(version -> !version.isPublished())) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }

        validateRequiredTermsAgreed(agreedByVersionId);

        return versions.stream()
                .collect(Collectors.toMap(version -> version, version -> agreedByVersionId.get(version.getId())));
    }

    private void validateRequiredTermsAgreed(Map<Long, Boolean> agreedByVersionId) {
        List<TermsType> requiredTypes = termsTypeRepository.findAllByIsRequiredTrue();
        Map<Long, TermsVersion> publishedByTypeId = getPublishedVersionsByType(requiredTypes);

        for (TermsType type : requiredTypes) {
            TermsVersion published = getPublishedVersionOrThrow(type, publishedByTypeId);
            Boolean agreed = agreedByVersionId.get(published.getId());

            if (agreed == null || !agreed) {
                throw new AgreementException(AgreementErrorCode.REQUIRED_TERM_NOT_AGREED);
            }
        }
    }

    // 약관 종류 목록에 대한 게시(PUBLISHED) 버전을 한 번의 쿼리로 일괄 조회
    private Map<Long, TermsVersion> getPublishedVersionsByType(List<TermsType> types) {
        return termsVersionRepository.findAllByTermsTypeInAndStatus(types, TermsVersionStatus.PUBLISHED).stream()
                .collect(Collectors.toMap(version -> version.getTermsType().getId(), version -> version));
    }

    private TermsVersion getPublishedVersionOrThrow(TermsType type, Map<Long, TermsVersion> publishedByTypeId) {
        TermsVersion version = publishedByTypeId.get(type.getId());
        if (version == null) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }
        return version;
    }
}
