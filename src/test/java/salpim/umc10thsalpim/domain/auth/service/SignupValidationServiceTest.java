package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupValidationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private SignupValidationService signupValidationService;

    @Test
    void returnsAdministrativeAreaAsSignupRegion() {
        Region administrativeArea = Region.builder()
                .id(1L)
                .name("Hwajeong-dong")
                .regionLevel(RegionLevel.ADMINISTRATIVE_AREA)
                .build();
        when(regionRepository.findById(1L)).thenReturn(Optional.of(administrativeArea));

        Region result = signupValidationService.findLeafRegion(1L);

        assertThat(result).isSameAs(administrativeArea);
    }

    @Test
    void rejectsGeneralGuAsSignupRegion() {
        Region generalGu = Region.builder()
                .id(2L)
                .name("Deogyang-gu")
                .regionLevel(RegionLevel.GENERAL_GU)
                .build();
        when(regionRepository.findById(2L)).thenReturn(Optional.of(generalGu));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> signupValidationService.findLeafRegion(2L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_LEAF);
    }
}
