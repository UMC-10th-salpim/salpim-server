package salpim.umc10thsalpim.domain.region.dto;

import lombok.Builder;

public class RegionResDTO {

    @Builder
    public record ResolveResult(
            Long regionId,
            String regionName,
            String fullRegionName
    ) {
    }
}
