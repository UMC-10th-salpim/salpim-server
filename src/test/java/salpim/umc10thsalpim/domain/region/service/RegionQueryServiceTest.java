package salpim.umc10thsalpim.domain.region.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionQueryService regionQueryService;

    @Test
    void getsSidoAndSigunguFromThreeLevelHierarchy() {
        Region sido = region(1L, null, "Incheon", RegionLevel.SIDO);
        Region sigungu = region(2L, 1L, "Michuhol-gu", RegionLevel.SIGUNGU);
        Region administrativeArea = region(3L, 2L, "Yonghyeon-dong", RegionLevel.ADMINISTRATIVE_AREA);

        given(regionRepository.findById(3L)).willReturn(Optional.of(administrativeArea));
        given(regionRepository.findById(2L)).willReturn(Optional.of(sigungu));
        given(regionRepository.findById(1L)).willReturn(Optional.of(sido));

        String[] result = regionQueryService.getSidoAndSigungu(3L);

        assertThat(result).containsExactly("Incheon", "Michuhol-gu");
    }

    @Test
    void getsSidoAndSigunguFromFourLevelHierarchy() {
        Region sido = region(1L, null, "Gyeonggi-do", RegionLevel.SIDO);
        Region sigungu = region(2L, 1L, "Goyang-si", RegionLevel.SIGUNGU);
        Region generalGu = region(3L, 2L, "Deogyang-gu", RegionLevel.GENERAL_GU);
        Region administrativeArea = region(4L, 3L, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);

        given(regionRepository.findById(4L)).willReturn(Optional.of(administrativeArea));
        given(regionRepository.findById(3L)).willReturn(Optional.of(generalGu));
        given(regionRepository.findById(2L)).willReturn(Optional.of(sigungu));
        given(regionRepository.findById(1L)).willReturn(Optional.of(sido));

        String[] result = regionQueryService.getSidoAndSigungu(4L);

        assertThat(result).containsExactly("Gyeonggi-do", "Goyang-si");
        assertThat(regionQueryService.findAncestorRegion(administrativeArea, RegionLevel.GENERAL_GU))
                .isSameAs(generalGu);
    }

    @Test
    void throwsExceptionWhenRequiredAncestorIsMissing() {
        Region administrativeArea = region(3L, null, "Yonghyeon-dong", RegionLevel.ADMINISTRATIVE_AREA);

        given(regionRepository.findById(3L)).willReturn(Optional.of(administrativeArea));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> regionQueryService.getSidoAndSigungu(3L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_HIERARCHY_INVALID);
    }

    @Test
    void throwsExceptionWhenRegionHierarchyHasCycle() {
        Region administrativeArea = region(3L, 2L, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);
        Region generalGu = region(2L, 3L, "Deogyang-gu", RegionLevel.GENERAL_GU);

        given(regionRepository.findById(3L)).willReturn(Optional.of(administrativeArea));
        given(regionRepository.findById(2L)).willReturn(Optional.of(generalGu));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> regionQueryService.getSidoAndSigungu(3L)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_HIERARCHY_INVALID);
    }

    private Region region(Long id, Long parentId, String name, RegionLevel regionLevel) {
        return Region.builder()
                .id(id)
                .parentId(parentId)
                .name(name)
                .regionLevel(regionLevel)
                .build();
    }
}
