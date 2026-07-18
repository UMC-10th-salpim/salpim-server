package salpim.umc10thsalpim.domain.benefit.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenefitConverterTest {

    @Test
    @DisplayName("WelfareBenefit과 판단 결과를 GetApplicationHelperInfo로 정확히 변환")
    void convertsWelfareBenefitToGetApplicationHelperInfo() {
        WelfareBenefit welfareBenefit = WelfareBenefit.builder()
                .id(100L)
                .title("테스트 혜택")
                .targetDescription("지원 대상")
                .applicationMethod("주민센터 방문")
                .applicationUrl("https://example.com")
                .contact("129")
                .organization("테스트 기관")
                .applicationEndDate(LocalDate.of(2026, 12, 31))
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(100)
                .build();

        List<ApplicationType> applicationTypeList = List.of(ApplicationType.ONLINE, ApplicationType.VISIT);

        BenefitResDTO.GetApplicationHelperInfo result = BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit,
                true,
                applicationTypeList,
                true,
                true
        );

        assertThat(result.benefitId()).isEqualTo(100L);
        assertThat(result.title()).isEqualTo("테스트 혜택");
        assertThat(result.targetDescription()).isEqualTo("지원 대상");
        assertThat(result.applicationMethod()).isEqualTo("주민센터 방문");
        assertThat(result.applicationUrl()).isEqualTo("https://example.com");
        assertThat(result.contact()).isEqualTo("129");
        assertThat(result.organization()).isEqualTo("테스트 기관");
        assertThat(result.isOnlineApplicationAvailable()).isTrue();
        assertThat(result.applicationTypeList()).containsExactly(ApplicationType.ONLINE, ApplicationType.VISIT);
        assertThat(result.applicationEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.isRegionSatisfied()).isTrue();
        assertThat(result.ageConditionStatus()).isEqualTo(AgeConditionStatus.RESTRICTED);
        assertThat(result.minAge()).isEqualTo(65);
        assertThat(result.maxAge()).isEqualTo(100);
        assertThat(result.isAgeSatisfied()).isTrue();
    }

    @Test
    @DisplayName("연령 조건을 판단할 수 없는 경우 isAgeSatisfied는 null로 변환")
    void convertsNullIsAgeSatisfiedWhenAgeConditionIsUnknown() {
        WelfareBenefit welfareBenefit = WelfareBenefit.builder()
                .id(101L)
                .title("연령 판단 불가 혜택")
                .ageConditionStatus(AgeConditionStatus.UNKNOWN)
                .build();

        BenefitResDTO.GetApplicationHelperInfo result = BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit,
                false,
                List.of(ApplicationType.PHONE),
                true,
                null
        );

        assertThat(result.ageConditionStatus()).isEqualTo(AgeConditionStatus.UNKNOWN);
        assertThat(result.isAgeSatisfied()).isNull();
        assertThat(result.minAge()).isNull();
        assertThat(result.maxAge()).isNull();
        assertThat(result.isOnlineApplicationAvailable()).isFalse();
        assertThat(result.applicationTypeList()).containsExactly(ApplicationType.PHONE);
    }

    @Test
    @DisplayName("지역 조건을 충족하지 못한 경우 isRegionSatisfied는 false로 변환")
    void convertsFalseIsRegionSatisfiedWhenRegionConditionNotMet() {
        WelfareBenefit welfareBenefit = WelfareBenefit.builder()
                .id(102L)
                .title("지역 조건 불충족 혜택")
                .build();

        BenefitResDTO.GetApplicationHelperInfo result = BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit,
                true,
                List.of(ApplicationType.ONLINE),
                false,
                true
        );

        assertThat(result.isRegionSatisfied()).isFalse();
    }
}