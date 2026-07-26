package salpim.umc10thsalpim.domain.region.converter;

import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.entity.Region;

import java.util.List;

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

    public static RegionResDTO.RegionListDTO toRegionResult(List<Region> regions) {
        return RegionResDTO.RegionListDTO.builder()
                .regionList(regions.stream()
                        .map( region ->
                            RegionResDTO.RegionDTO.builder()
                                    .regionId(region.getId())
                                    .regionName(region.getName())
                                    .build()
                        ).toList())
                .build();
    }
}
