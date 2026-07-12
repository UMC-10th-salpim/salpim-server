package salpim.umc10thsalpim.domain.benefit.dto;

public class BenefitResDTO {

    public record GetApplicationHelperInfo(
            Long benefitId,
            String title,
            String targetDescription,
            String applicationMethod,
            String applicationUrl,
            String contact,
            String organization
    ) {}
}
