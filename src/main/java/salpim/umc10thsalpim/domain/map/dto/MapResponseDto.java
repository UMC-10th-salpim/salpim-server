package salpim.umc10thsalpim.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class MapResponseDto {

    @Builder
    public record FacilityInfoResponseDto(
            @Schema(description = "시설 이름", example = "학익1동 행정복지센터")
            String name,
            @Schema(description = "시설 주소", example = "인천광역시 미추홀구 매소홀로 381")
            String address,
            @Schema(description = "운영 시간", example = "09:00 - 18:00")
            String hour,
            @Schema(description = "사용자 위치로부터의 거리", example = "1.2km")
            String distanceText,
            @Schema(description = "관할 행정복지센터 일치 여부", example = "true")
            boolean isMyCenter,
            @Schema(description = "혜택 페이징 객체")
            BenefitPageDto benefits
    ){}

    @Builder
    public record BenefitPageDto(
            @Schema(description = "혜택 데이터 리스트")
            List<BenefitDto> data,
            @Schema(description = "다음 페이지 존재 여부", example = "true")
            boolean hasNext,
            @Schema(description = "다음 요청 시 사용할 커서 ID", example = "WLF00001234")
            String nextCursor,
            @Schema(description = "현재 페이지 데이터 개수", example = "10")
            int pageSize,
            @Schema(description = "전체 혜택 데이터 총 개수", example = "42")
            int totalCount
    ){}

    @Builder
    public record BenefitDto(
            @Schema(description = "서비스 고유 ID (커서 식별자)", example = "WLF00001234")
            String servId,
            @Schema(description = "제공 지역", example = "전국")
            String region,
            @Schema(description = "서비스명", example = "청년월세 한시 특별지원")
            String serviceName
    ){
        public String getServId() {
            return servId;
        }
    }
}

