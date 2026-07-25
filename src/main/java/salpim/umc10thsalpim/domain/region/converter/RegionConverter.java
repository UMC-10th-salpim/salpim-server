package salpim.umc10thsalpim.domain.region.converter;

import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.entity.Region;

public final class RegionConverter {

    private RegionConverter() {
    }

    public static RegionResDTO.ResolveResult toResolveResult(Region region, String fullRegionName) {
        return RegionResDTO.ResolveResult.builder()
                .regionId(region.getId())
                .regionName(region.getName())
                .fullRegionName(fullRegionName)
                .build();
    }
}
