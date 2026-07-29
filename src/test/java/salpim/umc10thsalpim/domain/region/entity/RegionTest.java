package salpim.umc10thsalpim.domain.region.entity;

import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;

import static org.assertj.core.api.Assertions.assertThat;

class RegionTest {

    @Test
    void sidoCanHaveNoParent() {
        Region sido = Region.create(null, "Gyeonggi-do", RegionLevel.SIDO);

        assertThat(sido.getParent()).isNull();
        assertThat(sido.getRegionLevel()).isEqualTo(RegionLevel.SIDO);
    }

    @Test
    void administrativeAreaCanHaveGeneralGuAsParent() {
        Region sido = Region.create(null, "Gyeonggi-do", RegionLevel.SIDO);
        Region sigungu = Region.create(sido, "Goyang-si", RegionLevel.SIGUNGU);
        Region generalGu = Region.create(sigungu, "Deogyang-gu", RegionLevel.GENERAL_GU);
        Region administrativeArea = Region.create(generalGu, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);

        assertThat(administrativeArea.getParent()).isSameAs(generalGu);
        assertThat(generalGu.getParent()).isSameAs(sigungu);
        assertThat(administrativeArea.getRegionLevel()).isEqualTo(RegionLevel.ADMINISTRATIVE_AREA);
    }
}
