package salpim.umc10thsalpim.domain.member.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.entity.PasswordResetToken;
import salpim.umc10thsalpim.domain.auth.entity.PasswordVerificationAttempt;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.entity.RefreshToken;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.repository.PasswordResetTokenRepository;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.auth.repository.RefreshTokenRepository;
import salpim.umc10thsalpim.domain.benefit.entity.FavoriteBenefit;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.benefit.repository.FavoriteBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.term.entity.MemberAgreement;
import salpim.umc10thsalpim.domain.term.entity.TermsType;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;
import salpim.umc10thsalpim.domain.term.enums.TermsTypeCode;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsTypeRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsVersionRepository;

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
    private PhoneVerificationRepository phoneVerificationRepository;

    @Autowired
    private FavoriteBenefitRepository favoriteBenefitRepository;

    @Autowired
    private PasswordVerificationAttemptRepository passwordVerificationAttemptRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TermsTypeRepository termsTypeRepository;

    @Autowired
    private TermsVersionRepository termsVersionRepository;

    @Autowired
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Autowired
    private WelfareBenefitRepository welfareBenefitRepository;

    @Test
    void withdrawDeletesMemberOwnedDataButKeepsSharedData() {
        Region region = regionRepository.save(Region.create(null, "화전동", RegionLevel.ADMINISTRATIVE_AREA));
        WelfareBenefit benefit = welfareBenefitRepository.save(WelfareBenefit.builder()
                .externalId("benefit-1")
                .source("test")
                .title("benefit")
                .easySummary("easy summary")
                .whoCanReceive("who can receive")
                .whatYouReceive("what you receive")
                .recommendedFor("recommended for")
                .regionScope(RegionScope.NONE)
                .build());
        Member member = memberRepository.save(member(region, "01011112222"));
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .tokenHash("encoded-refresh-token")
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build());
        TermsType termsType = termsTypeRepository.save(TermsType.builder()
                .code(TermsTypeCode.SERVICE)
                .name("서비스 이용약관")
                .isRequired(true)
                .displayOrder(1)
                .build());
        TermsVersion termsVersion = termsVersionRepository.save(TermsVersion.builder()
                .termsType(termsType)
                .version("1.0.0")
                .effectiveDate(LocalDate.now())
                .build());
        memberTermAgreementRepository.save(MemberAgreement.builder()
                .member(member)
                .termsVersion(termsVersion)
                .agreed(true)
                .build());
        PhoneVerification phoneVerification = phoneVerificationRepository.save(
                PhoneVerification.builder()
                        .member(member)
                        .phoneNumber(member.getPhoneNumber())
                        .codeHash("encoded-code")
                        .expiredAt(LocalDateTime.now().plusMinutes(5))
                        .sentAt(LocalDateTime.now())
                        .verified(false)
                        .purpose(PhoneVerificationPurpose.PHONE_CHANGE)
                        .build()
        );
        FavoriteBenefit favoriteBenefit = favoriteBenefitRepository.save(FavoriteBenefit.builder()
                .memberId(member.getId())
                .benefitId(benefit.getId())
                .build());
        PasswordVerificationAttempt passwordVerificationAttempt =
                passwordVerificationAttemptRepository.save(
                        PasswordVerificationAttempt.create(
                                PasswordVerificationPurpose.PASSWORD_CHANGE,
                                PasswordVerificationTargetType.MEMBER,
                                member.getId().toString()
                        )
                );
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.save(
                PasswordResetToken.builder()
                        .member(member)
                        .tokenIdHash("password-reset-token-hash")
                        .expiredAt(LocalDateTime.now().plusMinutes(5))
                        .build()
        );

        memberWithdrawalService.withdraw(member.getId());
        memberRepository.flush();

        assertThat(memberRepository.existsById(member.getId())).isFalse();
        assertThat(refreshTokenRepository.existsById(refreshToken.getId())).isFalse();
        assertThat(memberTermAgreementRepository.count()).isZero();
        assertThat(phoneVerificationRepository.existsById(phoneVerification.getId())).isFalse();
        assertThat(favoriteBenefitRepository.existsById(favoriteBenefit.getId())).isFalse();
        assertThat(passwordVerificationAttemptRepository.existsById(passwordVerificationAttempt.getId()))
                .isFalse();
        assertThat(passwordResetTokenRepository.existsById(passwordResetToken.getId())).isFalse();
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
