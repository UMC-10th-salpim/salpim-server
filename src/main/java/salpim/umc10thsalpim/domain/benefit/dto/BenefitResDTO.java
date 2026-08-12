package salpim.umc10thsalpim.domain.benefit.dto;

import org.springframework.cglib.core.Local;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 혜택 요약, 자격, 혜택 내용, 내용
    @Builder
    public record GetBenefitDetailDTO(
        String title,
        String easySummary,
        String whoCanReceive,
        String whatYouReceive,
        String recommendedFor,
        LocalDate applicationStartDate,
        LocalDate applicationEndDate,
        String applicationUrl,
        String welfareCategoryName,
        Integer minAge,
        Integer maxAge,
        AgeConditionStatus ageConditionStatus,
        Boolean isOnlineApplicationAvailable
    ){}

    @Builder
    public record FavoriteBenefitDTO(
            Long benefitId,
            String title,
            LocalDate applicationEndDate,
            Integer minAge
    ){}

    @Builder
    public record FavoriteBenefitStatusDTO(
            Long benefitId,
            Boolean isFavorite
    ) {}

    @Builder
    public record DeadlineSoonBenefitDTO (
           Long benefitId,
           String title,
           LocalDate applicationEndDate,
           Integer dDay
    ){}

    @Builder
    public record BenefitShareDTO(
            String title,
            String summary
    ){}
}
