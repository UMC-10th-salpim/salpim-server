package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.exception.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.term.converter.AgreementConverter;
import salpim.umc10thsalpim.domain.term.entity.MemberTermAgreement;
import salpim.umc10thsalpim.domain.term.entity.Term;
import salpim.umc10thsalpim.domain.term.exception.AgreementErrorCode;
import salpim.umc10thsalpim.domain.term.exception.AgreementException;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;
import salpim.umc10thsalpim.domain.term.repository.TermRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalSignupService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final TermRepository termRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthReqDTO.LocalSignup request) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());

        validateDuplicatePhoneNumber(normalizedPhoneNumber);
        phoneVerificationService.validateVerifiedPhoneNumber(normalizedPhoneNumber);
        validateRegion(request.regionId());

        List<Term> agreedTerms = validateTerms(request.agreedTermIds());
        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = memberRepository.save(
                MemberConverter.toLocalMember(request, normalizedPhoneNumber, encodedPassword)
        );
        saveTermAgreements(member, agreedTerms);
        phoneVerificationService.deleteVerification(normalizedPhoneNumber);
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }

    private void validateRegion(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw new RegionException(RegionErrorCode.REGION_NOT_FOUND);
        }
    }

    private List<Term> validateTerms(List<Long> agreedTermIds) {
        Set<Long> distinctTermIds = new HashSet<>(agreedTermIds);
        if (distinctTermIds.contains(null)) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }

        List<Term> agreedTerms = termRepository.findAllById(distinctTermIds);
        if (agreedTerms.size() != distinctTermIds.size()) {
            throw new AgreementException(AgreementErrorCode.TERM_NOT_FOUND);
        }

        List<Term> requiredTerms = termRepository.findByRequiredTrue();
        boolean allRequiredAgreed = requiredTerms.stream()
                .map(Term::getId)
                .allMatch(distinctTermIds::contains);

        if (!allRequiredAgreed) {
            throw new AgreementException(AgreementErrorCode.REQUIRED_TERM_NOT_AGREED);
        }

        return agreedTerms;
    }

    private void saveTermAgreements(Member member, List<Term> agreedTerms) {
        List<MemberTermAgreement> agreements = agreedTerms.stream()
                .map(term -> AgreementConverter.toMemberTermAgreement(member, term))
                .toList();
        memberTermAgreementRepository.saveAll(agreements);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
