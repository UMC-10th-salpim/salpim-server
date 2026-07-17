package salpim.umc10thsalpim.domain.benefit.converter;

import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.util.List;

public class BenefitConverter {

    public static BenefitResDTO.GetApplicationHelperInfo toGetApplicationHelperInfo(
            WelfareBenefit welfareBenefit,
            Boolean isOnlineApplicationAvailable,
            List<ApplicationType> applicationTypeList,
            Boolean isRegionSatisfied
    ) {
        return new BenefitResDTO.GetApplicationHelperInfo(
                welfareBenefit.getId(),
                welfareBenefit.getTitle(),
                welfareBenefit.getTargetDescription(),
                welfareBenefit.getApplicationMethod(),
                welfareBenefit.getApplicationUrl(),
                welfareBenefit.getContact(),
                welfareBenefit.getOrganization(),
                isOnlineApplicationAvailable,
                applicationTypeList,
                welfareBenefit.getApplicationEndDate(),
                isRegionSatisfied
        );
    }
}
