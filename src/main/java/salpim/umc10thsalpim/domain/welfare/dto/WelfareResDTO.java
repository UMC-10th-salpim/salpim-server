package salpim.umc10thsalpim.domain.welfare.dto;

import lombok.Builder;

public class WelfareResDTO {

    @Builder
    public record WelfareSearchResultDTO(
            Long benefitId,
            String benefitTitle,
            String benefitCategory
    ) {}

}
