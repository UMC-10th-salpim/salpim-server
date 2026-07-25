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
    void resolveCreatesCityDistrictDongHierarchy() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve("Goyang", "Deogyang", "Hwajeon")
        );

        Region leaf = regionRepository.findById(result.regionId()).orElseThrow();
        Region district = leaf.getParent();
        Region city = district.getParent();

        assertThat(regionRepository.count()).isEqualTo(3);
        assertThat(leaf.getName()).isEqualTo("Hwajeon");
        assertThat(leaf.getRegionLevel()).isEqualTo(RegionLevel.EUP_MYEON_DONG);
        assertThat(district.getName()).isEqualTo("Deogyang");
        assertThat(district.getRegionLevel()).isEqualTo(RegionLevel.GU_GUN);
        assertThat(city.getName()).isEqualTo("Goyang");
        assertThat(city.getRegionLevel()).isEqualTo(RegionLevel.CITY);
        assertThat(city.getParent()).isNull();
        assertThat(result.fullRegionName()).isEqualTo("Goyang Deogyang Hwajeon");
    }

    @Test
    void resolveDoesNotCreateDuplicateRegionsForSameRequest() {
        RegionReqDTO.Resolve request = new RegionReqDTO.Resolve("Goyang", "Deogyang", "Hwajeon");

        RegionResDTO.ResolveResult first = regionService.resolve(request);
        RegionResDTO.ResolveResult second = regionService.resolve(request);

        assertThat(second.regionId()).isEqualTo(first.regionId());
        assertThat(regionRepository.count()).isEqualTo(3);
    }

    @Test
    void resolveSupportsDistrictDongHierarchyWithoutCity() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve(null, "Gangnam", "Yeoksam")
        );

        Region leaf = regionRepository.findById(result.regionId()).orElseThrow();
        Region district = leaf.getParent();

        assertThat(regionRepository.count()).isEqualTo(2);
        assertThat(leaf.getName()).isEqualTo("Yeoksam");
        assertThat(district.getName()).isEqualTo("Gangnam");
        assertThat(district.getParent()).isNull();
        assertThat(result.fullRegionName()).isEqualTo("Gangnam Yeoksam");
    }

    @Test
    void resolveSupportsDongOnlyHierarchy() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve(null, null, "Naseong")
        );

        Region leaf = regionRepository.findById(result.regionId()).orElseThrow();

        assertThat(regionRepository.count()).isEqualTo(1);
        assertThat(leaf.getName()).isEqualTo("Naseong");
        assertThat(leaf.getParent()).isNull();
        assertThat(leaf.getRegionLevel()).isEqualTo(RegionLevel.EUP_MYEON_DONG);
        assertThat(result.fullRegionName()).isEqualTo("Naseong");
    }

    @Test
    void resolveCreatesDifferentLeafRegionsWhenParentsAreDifferent() {
        RegionResDTO.ResolveResult first = regionService.resolve(
                new RegionReqDTO.Resolve("Suwon", "Paldal", "Jungang")
        );
        RegionResDTO.ResolveResult second = regionService.resolve(
                new RegionReqDTO.Resolve("Changwon", "Masanhappo", "Jungang")
        );

        List<Region> leaves = regionRepository.findAll().stream()
                .filter(region -> region.getRegionLevel() == RegionLevel.EUP_MYEON_DONG)
                .toList();

        assertThat(second.regionId()).isNotEqualTo(first.regionId());
        assertThat(leaves)
                .hasSize(2)
                .extracting(Region::getName)
                .containsExactly("Jungang", "Jungang");
    }

    @Test
    void resolveNormalizesBlankOptionalValuesAndRepeatedSpaces() {
        RegionResDTO.ResolveResult result = regionService.resolve(
                new RegionReqDTO.Resolve("  Sejong  ", "   ", " Na   seong ")
        );

        assertThat(result.fullRegionName()).isEqualTo("Sejong Na seong");
        assertThat(regionRepository.count()).isEqualTo(2);
    }
}
