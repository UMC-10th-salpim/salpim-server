package salpim.umc10thsalpim.domain.benefit.dto;

import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.time.LocalDate;
import java.util.List;

public class BenefitResDTO {

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
            Boolean isRegionSatisfied
    ) {}
}
