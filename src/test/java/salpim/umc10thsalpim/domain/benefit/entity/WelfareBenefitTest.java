package salpim.umc10thsalpim.domain.benefit.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;

import static org.assertj.core.api.Assertions.assertThat;

class WelfareBenefitTest {

    @Test
    @DisplayName("ageConditionStatus를 지정하지 않으면 기본값 UNKNOWN이 설정된다")
    void defaultsAgeConditionStatusToUnknownWhenNotSpecified() {
        WelfareBenefit welfareBenefit = WelfareBenefit.builder()
                .id(1L)
                .title("연령 조건 미지정 혜택")
                .build();

        assertThat(welfareBenefit.getAgeConditionStatus()).isEqualTo(AgeConditionStatus.UNKNOWN);
        assertThat(welfareBenefit.getMinAge()).isNull();
        assertThat(welfareBenefit.getMaxAge()).isNull();
    }

    @Test
    @DisplayName("ageConditionStatus를 명시적으로 지정하면 지정한 값이 설정된다")
    void usesExplicitlySpecifiedAgeConditionStatus() {
        WelfareBenefit welfareBenefit = WelfareBenefit.builder()
                .id(2L)
                .title("연령 제한 혜택")
                .ageConditionStatus(AgeConditionStatus.RESTRICTED)
                .minAge(65)
                .maxAge(80)
                .build();

        assertThat(welfareBenefit.getAgeConditionStatus()).isEqualTo(AgeConditionStatus.RESTRICTED);
        assertThat(welfareBenefit.getMinAge()).isEqualTo(65);
        assertThat(welfareBenefit.getMaxAge()).isEqualTo(80);
    }
}