package salpim.umc10thsalpim.domain.region.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RegionServiceTest {

    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void resolveCreatesThreeLevelHierarchyWithoutGeneralGu() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve("Incheon", "Michuhol-gu", null, "Yonghyeon-dong")
        );

        Region administrativeArea = regionRepository.findById(result.regionId()).orElseThrow();
        Region sigungu = administrativeArea.getParent();
        Region sido = sigungu.getParent();

        assertThat(regionRepository.count()).isEqualTo(3);
        assertThat(administrativeArea.getRegionLevel()).isEqualTo(RegionLevel.ADMINISTRATIVE_AREA);
        assertThat(sigungu.getRegionLevel()).isEqualTo(RegionLevel.SIGUNGU);
        assertThat(sido.getRegionLevel()).isEqualTo(RegionLevel.SIDO);
        assertThat(result.fullRegionName()).isEqualTo("Incheon Michuhol-gu Yonghyeon-dong");
    }

    @Test
    void resolveCreatesFourLevelHierarchyWithGeneralGu() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve("Gyeonggi-do", "Goyang-si", "Deogyang-gu", "Hwajeong-dong")
        );

        Region administrativeArea = regionRepository.findById(result.regionId()).orElseThrow();
        Region generalGu = administrativeArea.getParent();
        Region sigungu = generalGu.getParent();

        assertThat(regionRepository.count()).isEqualTo(4);
        assertThat(generalGu.getRegionLevel()).isEqualTo(RegionLevel.GENERAL_GU);
        assertThat(sigungu.getRegionLevel()).isEqualTo(RegionLevel.SIGUNGU);
        assertThat(result.fullRegionName()).isEqualTo("Gyeonggi-do Goyang-si Deogyang-gu Hwajeong-dong");
    }

    @Test
    void resolveReusesRegionsForSameRequest() {
        RegionReqDTO.Resolve request = new RegionReqDTO.Resolve(
                "Gyeonggi-do", "Goyang-si", "Deogyang-gu", "Hwajeong-dong"
        );

        RegionResDTO.ResolveResult first = regionService.resolve(request);
        RegionResDTO.ResolveResult second = regionService.resolve(request);

        assertThat(second.regionId()).isEqualTo(first.regionId());
        assertThat(regionRepository.count()).isEqualTo(4);
    }

    @Test
    void resolveSeparatesSameAdministrativeAreaNameUnderDifferentGeneralGu() {
        RegionResDTO.ResolveResult first = regionService.resolve(
                new RegionReqDTO.Resolve("Gyeonggi-do", "Goyang-si", "Deogyang-gu", "Jungang-dong")
        );
        RegionResDTO.ResolveResult second = regionService.resolve(
                new RegionReqDTO.Resolve("Gyeonggi-do", "Goyang-si", "Ilsandong-gu", "Jungang-dong")
        );

        List<Region> administrativeAreas = regionRepository.findAll().stream()
                .filter(region -> region.getRegionLevel() == RegionLevel.ADMINISTRATIVE_AREA)
                .toList();

        assertThat(first.regionId()).isNotEqualTo(second.regionId());
        assertThat(administrativeAreas)
                .hasSize(2)
                .extracting(Region::getName)
                .containsOnly("Jungang-dong");
    }

    @Test
    void resolveTreatsBlankGeneralGuAsAbsentAndNormalizesNames() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve("  Incheon  ", " Michuhol-gu ", "   ", " Yong  hyeon-dong ")
        );

        assertThat(regionRepository.count()).isEqualTo(3);
        assertThat(result.fullRegionName()).isEqualTo("Incheon Michuhol-gu Yong hyeon-dong");
    }
}
