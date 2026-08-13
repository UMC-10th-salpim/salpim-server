package salpim.umc10thsalpim.domain.benefit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class BenefitReqDTO {

   public record GetBenefitDetailDTO(
           @Schema(description = "조회할 복지 혜택 ID")
           Long benefitId
   ){}

    public record UpdateFavorite(
            @NotNull(message = "찜 상태는 필수입니다.")
            Boolean isFavorite
    ) {}
}
