package salpim.umc10thsalpim.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.repository.PasswordResetTokenRepository;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.benefit.repository.FavoriteBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final FavoriteBenefitRepository favoriteBenefitRepository;
    private final PasswordVerificationAttemptRepository passwordVerificationAttemptRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        phoneVerificationRepository.deleteByMember(member);
        favoriteBenefitRepository.deleteByMemberId(memberId);
        passwordVerificationAttemptRepository.deleteByTargetTypeAndTargetValue(
                PasswordVerificationTargetType.MEMBER,
                memberId.toString()
        );
        passwordResetTokenRepository.deleteByMemberId(memberId);

        refreshTokenRepository.deleteByMember(member);
        memberTermAgreementRepository.deleteByMember(member);
        memberRepository.delete(member);
    }
}
