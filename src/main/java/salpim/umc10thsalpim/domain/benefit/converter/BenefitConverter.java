package salpim.umc10thsalpim.domain.benefit.converter;

import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;

public class BenefitConverter {

    public static BenefitResDTO.GetApplicationHelperInfo toGetApplicationHelperInfo(
            WelfareBenefit welfareBenefit
    ) {
        return new BenefitResDTO.GetApplicationHelperInfo(
                welfareBenefit.getId(),
                welfareBenefit.getTitle(),
                welfareBenefit.getTargetDescription(),
                welfareBenefit.getApplicationMethod(),
                welfareBenefit.getApplicationUrl(),
                welfareBenefit.getContact(),
                welfareBenefit.getOrganization()
        );
    }
}
