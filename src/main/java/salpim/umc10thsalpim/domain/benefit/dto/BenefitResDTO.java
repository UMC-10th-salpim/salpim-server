package salpim.umc10thsalpim.domain.benefit.dto;

import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

public class BenefitResDTO {

    @Builder
    public record WelfareSearchResultDTO(
            Long benefitId,
            String benefitTitle,
            String benefitCategory
    ) {}

    public record GetApplicationHelperInfo(
            Long benefitId,
            String title,
            String targetDescription,
            String applicationMethod,
            String applicationUrl,
            String contact,
            String organization,
            Boolean isOnlineApplicationAvailable,
            List<ApplicationType> applicationTypeList,
            LocalDate applicationEndDate,
            Boolean isRegionSatisfied,
            AgeConditionStatus ageConditionStatus,
            Integer minAge,
            Integer maxAge,
            Boolean isAgeSatisfied
    ) {}

}
