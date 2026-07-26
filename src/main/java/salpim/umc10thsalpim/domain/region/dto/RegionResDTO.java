package salpim.umc10thsalpim.domain.region.dto;

import lombok.Builder;

import java.util.List;

public class RegionResDTO {

    @Builder
    public record ResolveResult(
            Long regionId,
            String regionName,
            String fullRegionName
    ) {
    }

    @Builder
    public record RegionDTO(
            Long regionId,
            String regionName
    ){}

    @Builder
    public record RegionListDTO(
            List<RegionDTO> regionList
    ){}
}
