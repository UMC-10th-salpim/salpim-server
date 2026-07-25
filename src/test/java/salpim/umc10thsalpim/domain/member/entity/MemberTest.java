package salpim.umc10thsalpim.domain.member.entity;

import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void memberCanReferenceRegion() {
        Region region = region();
        Member member = baseMemberBuilder()
                .loginType(SocialProvider.LOCAL)
                .password("encoded-password")
                .passwordRecoveryAnswer("answer")
                .region(region)
                .build();

        assertThat(member.getRegion()).isSameAs(region);
    }

    @Test
    void localMemberRequiresPassword() {
        Member member = baseMemberBuilder()
                .loginType(SocialProvider.LOCAL)
                .password(null)
                .passwordRecoveryAnswer("answer")
                .build();

        assertThatThrownBy(member::validateLoginTypeFields)
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.REQUIRED_LOCAL_PASSWORD));
    }

    @Test
    void localMemberRequiresPasswordRecoveryAnswer() {
        Member member = baseMemberBuilder()
                .loginType(SocialProvider.LOCAL)
                .password("encoded-password")
                .passwordRecoveryAnswer(" ")
                .build();

        assertThatThrownBy(member::validateLoginTypeFields)
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.REQUIRED_PASSWORD_RECOVERY_ANSWER));
    }

    @Test
    void kakaoMemberCanHaveNoPasswordAndNoPasswordRecoveryAnswer() {
        Member member = baseMemberBuilder()
                .loginType(SocialProvider.KAKAO)
                .password(null)
                .passwordRecoveryAnswer(null)
                .kakaoId("123456789")
                .build();

        assertThatCode(member::validateLoginTypeFields).doesNotThrowAnyException();
    }

    @Test
    void kakaoMemberRequiresKakaoId() {
        Member member = baseMemberBuilder()
                .loginType(SocialProvider.KAKAO)
                .kakaoId(null)
                .build();

        assertThatThrownBy(member::validateLoginTypeFields)
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.REQUIRED_KAKAO_ID));
    }

    private Member.MemberBuilder baseMemberBuilder() {
        return Member.builder()
                .phoneNumber("01031768867")
                .name("김지홍")
                .birthDate(LocalDate.of(2002, 3, 11))
                .gender(Gender.MALE)
                .roadAddress("경기 고양시 덕양구 화랑로 28")
                .latitude(BigDecimal.valueOf(37.1234567))
                .longitude(BigDecimal.valueOf(126.1234567))
                .region(region());
    }

    private Region region() {
        return Region.create(null, "화전동", RegionLevel.EUP_MYEON_DONG);
    }
}
