package salpim.umc10thsalpim.domain.benefit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.repository.BenefitRuleRepository;
import salpim.umc10thsalpim.domain.benefit.repository.FavoriteBenefitRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareCategoryRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);
    private static final Clock FIXED_KST_CLOCK = Clock.fixed(
            Instant.parse("2026-08-12T15:00:00Z"),
            KST
    );

    @Mock
    private BokjiroApiClient bokjiroApiClient;

    @Mock
    private WelfareBenefitRepository welfareBenefitRepository;

    @Mock
    private BenefitRuleRepository benefitRuleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionQueryService regionQueryService;

    @Mock
    private WelfareCategoryRepository welfareCategoryRepository;

    @Mock
    private FavoriteBenefitRepository favoriteBenefitRepository;

    private BenefitService benefitService;

    private BenefitRule onlineRule;

    @BeforeEach
    void setUp() {
        benefitService = new BenefitService(
                bokjiroApiClient,
                welfareBenefitRepository,
                benefitRuleRepository,
                regionRepository,
                memberRepository,
                welfareCategoryRepository,
                regionQueryService,
                favoriteBenefitRepository,
                FIXED_KST_CLOCK
        );
        onlineRule = BenefitRule.builder()
                .welfareBenefitId(100L)
                .applicationType(ApplicationType.ONLINE)
                .build();
    }

    @Test
    @DisplayName("회원 동과 헤택 동이 같으면 지역 조건 충족")
    void returnsTrueWhenMemberAdministrativeAreaMatchesBenefitAdministrativeArea() {
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(dongId)
                .regionScope(RegionScope.MEMBER_ADMINISTRATIVE_AREA)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(regionQueryService.findAncestorRegion(dong, RegionLevel.ADMINISTRATIVE_AREA))
                .willReturn(dong);
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
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
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(regionRepository.findById(sigunguId)).willReturn(Optional.of(sigungu));
        given(regionQueryService.findAncestorRegion(dong, RegionLevel.SIGUNGU))
                .willReturn(sigungu);
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    void returnsTrueWhenMemberUnderGeneralGuMatchesBenefitSigungu() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;
        Long sigunguId = 2L;

        Member member = Member.builder().id(memberId).regionId(memberRegionId).build();
        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .parentId(3L)
                .name("Hwajeong-dong")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();
        Region sigungu = Region.builder()
                .id(sigunguId)
                .parentId(1L)
                .name("Goyang-si")
                .regionLevel(RegionLevel.SIGUNGU)
                .build();
        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .regionId(sigunguId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .build();
        BenefitRule rule = BenefitRule.builder()
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(regionRepository.findById(sigunguId)).willReturn(Optional.of(sigungu));
        given(regionQueryService.findAncestorRegion(memberRegion, RegionLevel.SIGUNGU))
                .willReturn(sigungu);
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId)).willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    void returnsTrueWhenMemberGeneralGuMatchesBenefitGeneralGu() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;
        Long generalGuId = 3L;

        Member member = Member.builder().id(memberId).regionId(memberRegionId).build();
        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .parentId(generalGuId)
                .name("Hwajeong-dong")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();
        Region generalGu = Region.builder()
                .id(generalGuId)
                .parentId(2L)
                .name("Deogyang-gu")
                .regionLevel(RegionLevel.GENERAL_GU)
                .build();
        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .regionId(generalGuId)
                .regionScope(RegionScope.MEMBER_GENERAL_GU)
                .build();
        BenefitRule rule = BenefitRule.builder()
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberRegionId)).willReturn(Optional.of(memberRegion));
        given(regionRepository.findById(generalGuId)).willReturn(Optional.of(generalGu));
        given(regionQueryService.findAncestorRegion(memberRegion, RegionLevel.GENERAL_GU))
                .willReturn(generalGu);
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId)).willReturn(List.of(rule));

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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
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
                .regionScope(RegionScope.MEMBER_SIDO)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(dongId)).willReturn(Optional.of(dong));
        given(regionRepository.findById(sidoId)).willReturn(Optional.of(sido));
        given(regionQueryService.findAncestorRegion(dong, RegionLevel.SIDO))
                .willReturn(sido);
        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(rule));

        BenefitResDTO.GetApplicationHelperInfo result =
                benefitService.getApplicationHelperInfo(memberId, benefitId);

        assertThat(result.isRegionSatisfied()).isTrue();
    }

    @Test
    @DisplayName("회원 동과 혜택 동이 다르면 지역 조건 불충족")
    void returnsFalseWhenMemberAdministrativeAreaDoesNotMatchBenefitAdministrativeArea() {
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        Region benefitDong = Region.builder()
                .id(benefitDongId)
                .parentId(2L)
                .name("학익동")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("테스트 혜택")
                .regionId(benefitDongId)
                .regionScope(RegionScope.MEMBER_ADMINISTRATIVE_AREA)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.ONLINE)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(regionRepository.findById(memberDongId)).willReturn(Optional.of(memberDong));
        given(regionRepository.findById(benefitDongId)).willReturn(Optional.of(benefitDong));
        given(regionQueryService.findAncestorRegion(memberDong, RegionLevel.ADMINISTRATIVE_AREA))
                .willReturn(memberDong);
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("전국 대상 테스트 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("지역 정보 없는 테스트 혜택")
                .regionId(null)
                .regionScope(RegionScope.MEMBER_ADMINISTRATIVE_AREA)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
    @DisplayName("혜택 지역 레벨과 혜택 지역 범위가 다르면 예외 발생")
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        Region benefitDong = Region.builder()
                .id(benefitDongId)
                .parentId(2L)
                .name("학익동")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("지역 레벨 불일치 테스트 혜택")
                .regionId(benefitDongId)
                .regionScope(RegionScope.MEMBER_SIGUNGU)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("연령 조건 확인 불가 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.UNKNOWN)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("연령 제한 없는 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.NO_RESTRICTION)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
    void returnsTrueWhenKstClockDateIsMembersMinAgeBirthday() {
        Long memberId = 1L;
        Long benefitId = 100L;
        Long memberRegionId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .regionId(memberRegionId)
                .birthDate(TODAY.minusYears(65))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세 이상 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .birthDate(TODAY.minusYears(80))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세부터 80세 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .birthDate(TODAY.minusYears(64))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세 이상 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
                .birthDate(TODAY.minusYears(81))
                .build();

        Region memberRegion = Region.builder()
                .id(memberRegionId)
                .name("테스트 지역")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("65세부터 80세 혜택")
                .regionId(null)
                .regionScope(RegionScope.NONE)
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        BenefitRule rule = BenefitRule.builder()
                .id(1L)
                .welfareBenefitId(benefitId)
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
    @DisplayName("온라인 신청이 가능하고 신청 URL이 있으면 URL을 반환한다")
    void returnsOnlineApplicationUrl() {
        Long benefitId = 100L;
        String applicationUrl = "https://example.com/apply";

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .applicationUrl(applicationUrl)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(onlineRule));

        String result = benefitService.getOnlineApplicationUrl(benefitId);

        assertThat(result).isEqualTo(applicationUrl);
    }

    @Test
    @DisplayName("존재하지 않는 혜택의 온라인 신청 링크를 조회하면 예외가 발생한다")
    void throwsExceptionWhenBenefitIsNotFoundForOnlineApplication() {
        Long benefitId = 100L;

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.empty());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_NOT_FOUND);
    }

    @Test
    @DisplayName("혜택 신청 규칙이 없으면 온라인 신청 링크 조회 시 예외가 발생한다")
    void throwsExceptionWhenBenefitRuleIsNotFoundForOnlineApplication() {
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("온라인 신청 방식이 없으면 온라인 신청 링크 조회 시 예외가 발생한다")
    void throwsExceptionWhenOnlineApplicationIsNotAvailable() {
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .applicationUrl("https://example.com/apply")
                .build();

        BenefitRule visitRule = BenefitRule.builder()
                .welfareBenefitId(benefitId)
                .applicationType(ApplicationType.VISIT)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(visitRule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_ONLINE_APPLICATION_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("온라인 신청 방식이 있지만 신청 URL이 없으면 예외가 발생한다")
    void throwsExceptionWhenApplicationUrlIsNotConfigured() {
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(onlineRule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_APPLICATION_URL_NOT_CONFIGURED);
    }

    @Test
    @DisplayName("온라인 신청 URL 형식이 올바르지 않으면 예외가 발생한다")
    void throwsExceptionWhenApplicationUrlIsInvalid() {
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .applicationUrl("ftp://example.com/apply")
                .build();

        given(welfareBenefitRepository.findById(benefitId)).willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(onlineRule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_APPLICATION_URL_INVALID);
    }

    @Test
    @DisplayName("온라인 신청 URL에 host가 없으면 예외가 발생한다")
    void throwsExceptionWhenApplicationUrlHasNoHost() {
        Long benefitId = 100L;

        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .applicationUrl("https:apply")
                .build();

        given(welfareBenefitRepository.findById(benefitId))
                .willReturn(Optional.of(benefit));
        given(benefitRuleRepository.findAllByWelfareBenefitId(benefitId))
                .willReturn(List.of(onlineRule));

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getOnlineApplicationUrl(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_APPLICATION_URL_INVALID);
    }

    @Test
    @DisplayName("카카오톡 공유하기용 혜택 정보 정상 조회")
    void returnsBenefitShareInfo() {
        Long benefitId = 100L;
        WelfareBenefit benefit = WelfareBenefit.builder()
                .id(benefitId)
                .title("청년월세 특별지원")
                .easySummary("청년 가구에 월세를 지원합니다.")
                .build();

        given(welfareBenefitRepository.findById(benefitId))
                .willReturn(Optional.of(benefit));

        BenefitResDTO.BenefitShareDTO result = benefitService.getBenefitShareInfo(benefitId);

        assertThat(result.title()).isEqualTo("청년월세 특별지원");
        assertThat(result.summary()).isEqualTo("청년 가구에 월세를 지원합니다.");
    }

    @Test
    @DisplayName("카카오톡 공유하기용 혜택 조회 시 혜택이 없으면 예외가 발생한다")
    void throwsExceptionWhenBenefitNotFoundForShareInfo() {
        Long benefitId = 100L;

        given(welfareBenefitRepository.findById(benefitId))
                .willReturn(Optional.empty());

        BenefitException exception = assertThrows(
                BenefitException.class,
                () -> benefitService.getBenefitShareInfo(benefitId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(BenefitErrorCode.BENEFIT_NOT_FOUND);
    }
}
