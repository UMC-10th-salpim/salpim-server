package salpim.umc10thsalpim.domain.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegionReqDTO {

    public record Resolve(
            @NotBlank(message = "시/도 정보는 필수입니다.")
            @Size(max = 50, message = "시/도 정보는 50자 이하여야합니다.")
            String sido,

            @NotBlank(message = "시/군/구 정보는 필수입니다.")
            @Size(max = 50, message = "시/군/구 정보는 50자 이하여야합니다.")
            String sigungu,

            @Size(max = 50, message = "구 정보는 50자 이하여야합니다.")
            String generalGu,

            @NotBlank(message = "행정구역 정보는 필수입니다.")
            @Size(max = 50, message = "행정구역 정보는 50자 이하여야합니다.")
            String administrativeArea
    ) {
    }
}
