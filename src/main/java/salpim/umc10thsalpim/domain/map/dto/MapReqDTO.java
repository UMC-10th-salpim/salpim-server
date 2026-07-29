package salpim.umc10thsalpim.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

public class MapReqDTO {

    @Builder
    public record FacilityInfoRequest(
            @Schema(description = "Facility name", example = "Hakik 1-dong Administrative Welfare Center")
            @NotBlank(message = "facilityName is required.")
            String facilityName,

            @Schema(description = "Facility address", example = "381 Maesoho-ro, Michuhol-gu, Incheon")
            @NotBlank(message = "address is required.")
            String address,

            @Schema(description = "Latitude", example = "37.4475")
            @NotNull(message = "latitude is required.")
            @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90.")
            @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90.")
            BigDecimal latitude,

            @Schema(description = "Longitude", example = "126.6675")
            @NotNull(message = "longitude is required.")
            @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180.")
            @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180.")
            BigDecimal longitude,

            @Schema(description = "Cursor from the previous response", example = "WLF00001234")
            String cursor,

            @Schema(description = "Requested page size", example = "10")
            Integer size
    ) {
    }
}
