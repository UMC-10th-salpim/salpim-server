package salpim.umc10thsalpim.domain.benefit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.repository.BenefitRuleRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    @Mock
    private WelfareBenefitRepository welfareBenefitRepository;

    @Mock
    private BenefitRuleRepository benefitRuleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private BenefitService benefitService;

    @Test
    @DisplayName("회원 동과 헤택 동이 같으면 지역 조건 충족")
    void returnsTrueWhenMemberDongMatchesBenefitDong() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long dongId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(dongId)
                .build();

        Region dong = Region.builder()
                .id(dongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(dongId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_DONG)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("회원 시군구와 혜택 시군구가 같으면 지역 조건 충족")
    void returnsTrueWhenMemberSigunguMatchesBenefitSigungu(){
        Long memberId = 1L;
        Long benefitId = 100L;
        Long dongId = 10L;
        Long sigunguId = 2L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(dongId)
                .build();

        Region dong = Region.builder()
                .id(dongId)
                .parentId(sigunguId)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region sigungu = Region.builder()
                .id(sigunguId)
                .parentId(1L)
                .name("미추홀구")
                .regionLevel(RegionLevel.SIGUNGU)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(sigunguId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(regionRepository.findById(sigunguId)).willReturn(Optional.of(sigungu));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("회원 시도와 혜택 시도가 같으면 지역 조건 충족")
    void returnsTrueWhenMemberSidoMatchesBenefitSido() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long sidoId = 1L;
        Long sigunguId = 2L;
        Long dongId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(dongId)
                .build();

        Region dong = Region.builder()
                .id(dongId)
                .parentId(sigunguId)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region sigungu = Region.builder()
                .id(sigunguId)
                .parentId(sidoId)
                .name("미추홀구")
                .regionLevel(RegionLevel.SIGUNGU)
                .build();

        Region sido = Region.builder()
                .id(sidoId)
                .name("인천광역시")
                .regionLevel(RegionLevel.SIDO)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(sidoId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_SIDO)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(regionRepository.findById(sigunguId)).willReturn(Optional.of(sigungu));
        given(regionRepository.findById(sidoId)).willReturn(Optional.of(sido));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("회원 동과 혜택 동이 다르면 지역 조건 불충족")
    void returnsFalseWhenMemberDongDoesNotMatchBenefitDong() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;
        Long benefitDongId = 11L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region benefitDong = Region.builder()
                .id(benefitDongId)
                .parentId(2L)
                .name("학익동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(benefitDongId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_DONG)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(benefitDongId)).willReturn(Optional.of(benefitDong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isFalse();
    }

    @Test
    @DisplayName("지역 제한이 없는 혜택은 지역 조건을 충족")
    void returnsTrueWhenRegionScopeIsNone() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("전국 대상 테스트 혜택")
                .regionId(null)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("지역 조건이 있는데 혜택 지역 정보가 없으면 예외 발생")
    void throwsExceptionWhenBenefitRegionIsNotConfigured() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("지역 정보 없는 테스트 혜택")
                .regionId(null)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_DONG)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_REGION_NOT_CONFIGURED);
    }

    @Test
    @DisplayName("혜택 지역 레벨과 신청 규칙 범위가 다르면 예외 발생")
    void throwsExceptionWhenBenefitRegionLevelDoesNotMatchRegionScope() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;
        Long benefitDongId = 11L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region benefitDong = Region.builder()
                .id(benefitDongId)
                .parentId(2L)
                .name("학익동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("지역 레벨 불일치 테스트 혜택")
                .regionId(benefitDongId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(benefitDongId)).willReturn(Optional.of(benefitDong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_REGION_LEVEL_MISMATCH);
    }

    @Test
    @DisplayName("연령 조건을 확인할 수 없는 혜택은 연령 적합 여부 제공X")
    void returnsNullWhenAgeConditionIsUnknown() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.of(1960, 1, 1))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("연령 조건 확인 불가 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.UNKNOWN)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isNull();
    }

    @Test
    @DisplayName("연령 제한이 없는 혜택은 연령 조건을 충족")
    void returnsTrueWhenAgeConditionHasNoRestriction() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.of(1960, 1, 1))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("연령 제한 없는 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.NO_RESTRICTION)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isTrue();
    }

    @Test
    @DisplayName("제한 연령 혜택에서 회원 나이가 최소 연령과 같으면 연령 조건을 충족")
    void returnsTrueWhenAgeEqualsMinAge() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.now().minusYears(65))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세 이상 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isTrue();
    }

    @Test
    @DisplayName("제한 연령 혜택에서 회원 나이가 최대 연령과 같으면 연령 조건을 충족")
    void returnsTrueWhenAgeEqualsMaxAge() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.now().minusYears(80))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세부터 80세 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isTrue();
    }

    @Test
    @DisplayName("제한 연령 혜택에서 회원 나이가 최소 연령보다 작으면 연령 조건을 불충족")
    void returnsFalseWhenAgeIsBelowMinAge() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.now().minusYears(64))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세 이상 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isFalse();
    }

    @Test
    @DisplayName("제한 연령 혜택에서 회원 나이가 최대 연령보다 크면 연령 조건을 불충족")
    void returnsFalseWhenAgeExceedsMaxAge() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(LocalDate.now().minusYears(81))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세부터 80세 혜택")
                .regionId(null)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isAgeSatisfied()).isFalse();
    }

    @Test
    @DisplayName("혜택을 찾을 수 없으면 예외 발생")
    void throwsExceptionWhenWelfareBenefitNotFound() {
        Long memberId = 1L;
        Long benefitId = 100L;

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.empty());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_NOT_FOUND);
    }

    @Test
    @DisplayName("혜택의 신청 규칙이 존재하지 않으면 예외 발생")
    void throwsExceptionWhenBenefitRuleListIsEmpty() {
        Long memberId = 1L;
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("신청 규칙 없는 혜택")
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("회원을 찾을 수 없으면 예외 발생")
    void throwsExceptionWhenMemberNotFound() {
        Long memberId = 1L;
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        MemberException exception = assertThrows(
                MemberException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원의 지역 정보를 찾을 수 없으면 예외 발생")
    void throwsExceptionWhenMemberRegionNotFound() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.empty());

        RegionException exception = assertThrows(
                RegionException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_NOT_FOUND);
    }

    @Test
    @DisplayName("ONLINE 신청 규칙이 없으면 온라인 신청 불가로 판단")
    void returnsFalseForOnlineApplicationAvailableWhenNoOnlineRuleExists() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("방문/전화 신청만 가능한 혜택")
                .build();

        BenefitRule visitRule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.VISIT)
                .build();

        BenefitRule phoneRule = BenefitRule.builder()
                .id(2L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.PHONE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(visitRule, phoneRule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isOnlineApplicationAvailable()).isFalse();
        assertThat(result.applicationTypeList())
                .containsExactly(ApplicationType.VISIT, ApplicationType.PHONE);
    }

    @Test
    @DisplayName("동일한 신청 방식의 신청 규칙이 여러 개여도 신청 방식 목록은 중복 제거되어 반환")
    void returnsDistinctApplicationTypesWhenDuplicateRulesExist() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("중복 신청 규칙 혜택")
                .build();

        BenefitRule onlineRuleA = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        BenefitRule onlineRuleB = BenefitRule.builder()
                .id(2L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(onlineRuleA, onlineRuleB));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.applicationTypeList()).containsExactly(ApplicationType.ONLINE);
        assertThat(result.isOnlineApplicationAvailable()).isTrue();
    }

    @Test
    @DisplayName("여러 신청 규칙 중 하나라도 지역 조건을 충족하면 지역 조건 충족으로 판단")
    void returnsTrueWhenAnyRuleAmongMultipleSatisfiesRegionCondition() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;
        Long benefitDongId = 11L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(2L)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region benefitDong = Region.builder()
                .id(benefitDongId)
                .parentId(2L)
                .name("학익동")
                .regionLevel(RegionLevel.DONG)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("복수 신청 규칙 혜택")
                .regionId(benefitDongId)
                .build();

        BenefitRule unmatchedDongRule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_DONG)
                .applicationType(ApplicationType.VISIT)
                .build();

        BenefitRule noneScopedRule = BenefitRule.builder()
                .id(2L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.NONE)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(benefitDongId)).willReturn(Optional.of(benefitDong));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(unmatchedDongRule, noneScopedRule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("회원의 지역 계층에서 목표 레벨의 조상을 찾지 못하면 지역 조건 불충족")
    void returnsFalseWhenMemberRegionHierarchyDoesNotReachTargetLevel() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;
        Long benefitSigunguId = 2L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(null)
                .name("고아 동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region benefitSigungu = Region.builder()
                .id(benefitSigunguId)
                .parentId(1L)
                .name("미추홀구")
                .regionLevel(RegionLevel.SIGUNGU)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("시군구 조건 혜택")
                .regionId(benefitSigunguId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(benefitSigunguId)).willReturn(Optional.of(benefitSigungu));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isFalse();
    }

    @Test
    @DisplayName("회원 지역의 상위 지역을 찾을 수 없으면 예외 발생")
    void throwsExceptionWhenAncestorRegionInChainIsMissing() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberDongId = 10L;
        Long missingSigunguId = 2L;
        Long benefitSigunguId = 3L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberDongId)
                .build();

        Region memberDong = Region.builder()
                .id(memberDongId)
                .parentId(missingSigunguId)
                .name("용현동")
                .regionLevel(RegionLevel.DONG)
                .build();

        Region benefitSigungu = Region.builder()
                .id(benefitSigunguId)
                .parentId(1L)
                .name("미추홀구")
                .regionLevel(RegionLevel.SIGUNGU)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("시군구 조건 혜택")
                .regionId(benefitSigunguId)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(missingSigunguId)).willReturn(Optional.empty());
        given(regionRepository.findById(benefitSigunguId)).willReturn(Optional.of(benefitSigungu));
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> benefitService.getApplicationHelperInfo(memberId, benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_NOT_FOUND);
    }
}