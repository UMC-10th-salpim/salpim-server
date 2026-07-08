package salpim.umc10thsalpim.domain.welfare.dto;

import lombok.Builder;

import java.util.List;

public class WelfareResDTO {

    @Builder
    public record WelfareSearchResultDTO(
            Long benefitId,
            String benefitTitle,
            String benefitCategory
    ) {}

}
