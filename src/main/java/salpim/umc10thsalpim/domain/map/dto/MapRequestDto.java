package salpim.umc10thsalpim.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

public class MapRequestDto {

    @Builder
    public record FacilityInfoRequest(
        @Schema(description = "시설 이름", example = "학익1동 행정복지센터")
        @NotBlank(message = "시설 이름은 필수입니다.")
        String facilityName,

        @Schema(description = "주소", example = "인천광역시 미추홀구 매소홀로 381")
        @NotBlank(message = "주소는 필수입니다.")
        String address,

        //클라이언트 요청에서 검증
        @Schema(description = "위도", example = "37.4475")
        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "유효하지 않은 위도 값입니다.")
        @DecimalMax(value = "90.0", message = "유효하지 않은 위도 값입니다.")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.6675")
        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "유효하지 않은 경도 값입니다.")
        @DecimalMax(value = "180.0", message = "유효하지 않은 경도 값입니다.")
        BigDecimal longitude,

        @Schema(description = "페이징 커서 (이전 페이지 응답의 nextCursor 값)", example = "WLF00001234")
        String cursor,

        @Schema(description = "페이지 당 가져올 데이터 수", example = "10")
        Integer size
    ){}
}
