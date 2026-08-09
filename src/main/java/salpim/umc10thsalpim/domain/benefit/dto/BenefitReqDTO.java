package salpim.umc10thsalpim.domain.benefit.dto;

import jakarta.validation.constraints.NotNull;

public class BenefitReqDTO {

   public record GetBenefitDetailDTO(
       Long benefitId
   ){}

    public record UpdateFavorite(
            @NotNull(message = "찜 상태는 필수입니다.")
            Boolean isFavorite
    ) {}
}
