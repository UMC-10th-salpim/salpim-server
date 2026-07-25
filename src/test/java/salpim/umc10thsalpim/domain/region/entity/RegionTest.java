package salpim.umc10thsalpim.domain.region.entity;

import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;

import static org.assertj.core.api.Assertions.assertThat;

class RegionTest {

    @Test
    void topLevelRegionCanHaveNoParent() {
        Region sido = Region.create(null, "경기도", RegionLevel.SIDO);

        assertThat(sido.getParent()).isNull();
        assertThat(sido.getName()).isEqualTo("경기도");
        assertThat(sido.getRegionLevel()).isEqualTo(RegionLevel.SIDO);
    }

    @Test
    void childRegionCanHaveParentRegion() {
        Region sido = Region.create(null, "경기도", RegionLevel.SIDO);
        Region city = Region.create(sido, "고양시", RegionLevel.CITY);

        assertThat(city.getParent()).isSameAs(sido);
        assertThat(city.getRegionLevel()).isEqualTo(RegionLevel.CITY);
    }
}
