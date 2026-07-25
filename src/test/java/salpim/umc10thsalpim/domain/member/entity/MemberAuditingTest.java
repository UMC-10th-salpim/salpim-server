package salpim.umc10thsalpim.domain.member.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberAuditingTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void createdAtAndUpdatedAtAreSavedWhenMemberIsPersisted() {
        Region region = regionRepository.saveAndFlush(
                Region.create(null, "Hwajeon", RegionLevel.EUP_MYEON_DONG)
        );
        Member member = Member.builder()
                .loginType(SocialProvider.LOCAL)
                .phoneNumber("01012345678")
                .password("encoded-password")
                .name("Jihong")
                .birthDate(LocalDate.of(2002, 3, 11))
                .gender(Gender.MALE)
                .roadAddress("Goyang Deogyang Hwarang-ro 28")
                .latitude(BigDecimal.valueOf(37.1234567))
                .longitude(BigDecimal.valueOf(126.1234567))
                .region(region)
                .passwordRecoveryAnswer("Seoul")
                .build();

        Member savedMember = memberRepository.saveAndFlush(member);

        assertThat(savedMember.getCreatedAt()).isNotNull();
        assertThat(savedMember.getUpdatedAt()).isNotNull();
    }
}
