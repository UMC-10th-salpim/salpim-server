package salpim.umc10thsalpim.domain.benefit.dto;

import lombok.Builder;

public class BenefitResDTO {

    @Builder
    public record WelfareSearchResultDTO(
            Long benefitId,
            String benefitTitle,
            String benefitCategory
    ) {}

}
