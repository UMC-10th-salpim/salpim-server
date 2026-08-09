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
            @Schema(description = "시설이름", example = "학익1동 행정복지센터")
            @NotBlank
            String facilityName,

            @Schema(description = "주소", example = "인천 미추홀구 매소홀로")
            @NotBlank
            String address,

            @Schema(description = "Latitude", example = "37.4475")
            @NotNull
            @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90.")
            @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90.")
            BigDecimal latitude,

            @Schema(description = "Longitude", example = "126.6675")
            @NotNull
            @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180.")
            @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180.")
            BigDecimal longitude,

            @Schema(example = "306")
            String cursor,

            @Schema(example = "10")
            Integer size
    ) {
    }
}
