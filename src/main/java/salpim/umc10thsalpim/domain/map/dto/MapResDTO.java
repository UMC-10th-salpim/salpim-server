package salpim.umc10thsalpim.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class MapResDTO {

    @Builder
    public record FacilityInfoResDTO(
            @Schema(description = "Facility name", example = "Hakik 1-dong Administrative Welfare Center")
            String name,
            @Schema(description = "Facility address", example = "381 Maesoho-ro, Michuhol-gu, Incheon")
            String address,
            @Schema(description = "Operating hours", example = "09:00 - 18:00")
            String hour,
            @Schema(description = "Distance from the member", example = "1.2km")
            String distanceText,
            @Schema(description = "Whether this is the member's service center", example = "true")
            boolean isMyCenter,
            @Schema(description = "Paginated benefits")
            BenefitPageDTO benefits
    ) {
    }

    @Builder
    public record BenefitPageDTO(
            @Schema(description = "Benefit list")
            List<BenefitDTO> data,
            @Schema(description = "Whether another page exists", example = "true")
            boolean hasNext,
            @Schema(description = "Cursor for the next request", example = "WLF00001234")
            String nextCursor,
            @Schema(description = "Number of benefits in this page", example = "10")
            int pageSize,
            @Schema(description = "Total benefit count", example = "42")
            int totalCount
    ) {
    }

    @Builder
    public record BenefitDTO(
            @Schema(description = "Service identifier used as a cursor", example = "WLF00001234")
            String servId,
            @Schema(description = "Provided region", example = "Nationwide")
            String region,
            @Schema(description = "Service name", example = "Youth monthly rent support")
            String serviceName
    ) {
        public String getServId() {
            return servId;
        }
    }
}
