package salpim.umc10thsalpim.domain.member.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.term.entity.MemberTermAgreement;
import salpim.umc10thsalpim.domain.term.entity.Term;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;
import salpim.umc10thsalpim.domain.term.repository.TermRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberWithdrawalServiceTest {

    @Autowired
    private MemberWithdrawalService memberWithdrawalService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Autowired
    private WelfareBenefitRepository welfareBenefitRepository;

    @Test
    void withdrawDeletesMemberOwnedDataButKeepsSharedData() {
        Region region = regionRepository.save(Region.create(null, "화전동", RegionLevel.EUP_MYEON_DONG));
        WelfareBenefit benefit = welfareBenefitRepository.save(WelfareBenefit.builder()
                .externalId("benefit-1")
                .source("test")
                .title("benefit")
                .content("content")
                .regionScope(RegionScope.NONE)
                .build());
        Member member = memberRepository.save(member(region, "01011112222"));
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .token("refresh-token")
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build());
        Term term = termRepository.save(Term.builder()
                .title("terms")
                .required(true)
                .build());
        memberTermAgreementRepository.save(MemberTermAgreement.builder()
                .member(member)
                .term(term)
                .agreedAt(LocalDateTime.now())
                .build());

        memberWithdrawalService.withdraw(member.getId());
        memberRepository.flush();

        assertThat(memberRepository.existsById(member.getId())).isFalse();
        assertThat(refreshTokenRepository.existsById(refreshToken.getId())).isFalse();
        assertThat(memberTermAgreementRepository.count()).isZero();
        assertThat(regionRepository.existsById(region.getId())).isTrue();
        assertThat(welfareBenefitRepository.existsById(benefit.getId())).isTrue();
    }

    @Test
    void withdrawThrowsWhenMemberDoesNotExist() {
        assertThatThrownBy(() -> memberWithdrawalService.withdraw(-1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Member member(Region region, String phoneNumber) {
        return Member.builder()
                .loginType(SocialProvider.LOCAL)
                .phoneNumber(phoneNumber)
                .password("encoded-password")
                .name("김지홍")
                .birthDate(LocalDate.of(2002, 3, 11))
                .gender(Gender.MALE)
                .roadAddress("경기도 고양시 덕양구 화랑로 28")
                .latitude(BigDecimal.valueOf(37.1234567))
                .longitude(BigDecimal.valueOf(126.1234567))
                .region(region)
                .passwordRecoveryAnswer("가을")
                .welfareCenter(region.getName())
                .build();
    }
}
