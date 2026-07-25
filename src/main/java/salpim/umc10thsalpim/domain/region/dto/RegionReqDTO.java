package salpim.umc10thsalpim.domain.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegionReqDTO {

    public record Resolve(
            @Size(max = 50, message = "city must be 50 characters or less.")
            String city,

            @Size(max = 50, message = "district must be 50 characters or less.")
            String district,

            @NotBlank(message = "eupMyeonDong is required.")
            @Size(max = 50, message = "eupMyeonDong must be 50 characters or less.")
            String eupMyeonDong
    ) {
    }
}
