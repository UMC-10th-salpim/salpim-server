package salpim.umc10thsalpim.domain.benefit.converter;

import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.global.dto.CursorResDTO;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class BenefitConverter {

   public static CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> toPagination(List<WelfareBenefit> page, String nextCursor, Integer totalCount, Map<Long, String> categoryNameMap) {
      return CursorResDTO.Pagination.<BenefitResDTO.WelfareSearchResultDTO>builder()
          .data(page.stream()
              .map(b -> toWelfareSearchRes(b,
                  categoryNameMap.get(b.getCategoryId())))
              .toList())
          .hasNext(nextCursor != null)
          .nextCursor(nextCursor)
          .totalCount(totalCount)
          .pageSize(page.size())
          .build();
   }

   public static BenefitResDTO.WelfareSearchResultDTO toWelfareSearchRes(WelfareBenefit benefit, String categoryName) {
      return BenefitResDTO.WelfareSearchResultDTO.builder()
          .benefitId(benefit.getId())
          .benefitTitle(benefit.getTitle())
          .benefitCategory(categoryName)
          .build();
   }

   public static BenefitResDTO.GetBenefitDetailDTO toGetBenefitDetailDTO(
       WelfareBenefit welfareBenefit,
       String categoryName,
       Boolean isOnlineApplicationAvailable
   ) {
      return BenefitResDTO.GetBenefitDetailDTO.builder()
          .title(welfareBenefit.getTitle())
          .easySummary(welfareBenefit.getEasySummary())
          .whoCanReceive(welfareBenefit.getWhoCanReceive())
          .whatYouReceive(welfareBenefit.getWhatYouReceive())
          .recommendedFor(welfareBenefit.getRecommendedFor())
          .applicationStartDate(welfareBenefit.getApplicationStartDate())
          .applicationEndDate(welfareBenefit.getApplicationEndDate())
          .applicationUrl(welfareBenefit.getApplicationUrl())
          .welfareCategoryName(categoryName)
          .minAge(welfareBenefit.getMinAge())
          .maxAge(welfareBenefit.getMaxAge())
          .ageConditionStatus(welfareBenefit.getAgeConditionStatus())
          .isOnlineApplicationAvailable(isOnlineApplicationAvailable)
          .build();
   }

   public static BenefitResDTO.GetApplicationHelperInfo toGetApplicationHelperInfo(
       WelfareBenefit welfareBenefit,
       Boolean isOnlineApplicationAvailable,
       List<ApplicationType> applicationTypeList,
       Boolean isRegionSatisfied,
       Boolean isAgeSatisfied
   ) {
      return new BenefitResDTO.GetApplicationHelperInfo(
          welfareBenefit.getId(),
          welfareBenefit.getTitle(),
          welfareBenefit.getApplicationUrl(),
          welfareBenefit.getContact(),
          welfareBenefit.getOrganization(),
          isOnlineApplicationAvailable,
          applicationTypeList,
          welfareBenefit.getApplicationEndDate(),
          isRegionSatisfied,
          welfareBenefit.getAgeConditionStatus(),
          welfareBenefit.getMinAge(),
          welfareBenefit.getMaxAge(),
          isAgeSatisfied
      );
   }

   public static CursorResDTO.Pagination<BenefitResDTO.FavoriteBenefitDTO> toFavoriteBenefitPagination(List<WelfareBenefit> favoriteBenefits, Long totalCount, Boolean hasNext,  Map<Long, String> categoryNameMap) {
      return CursorResDTO.Pagination.<BenefitResDTO.FavoriteBenefitDTO>builder()
          .data(favoriteBenefits.stream()
                  .map(benefit -> toFavoriteBenefitDTO(
                          benefit,
                          categoryNameMap.get(benefit.getCategoryId())
                  ))
                  .toList())
          .totalCount(totalCount.intValue())
          .pageSize(favoriteBenefits.size())
          .hasNext(hasNext)
          .build();
   }

   public static BenefitResDTO.FavoriteBenefitDTO toFavoriteBenefitDTO(WelfareBenefit benefit, String categoryName) {
      return BenefitResDTO.FavoriteBenefitDTO.builder()
          .benefitId(benefit.getId())
          .title(benefit.getTitle())
              .benefitCategory(categoryName)
          .applicationEndDate(benefit.getApplicationEndDate())
          .minAge(benefit.getMinAge())
          .build();
   }

   public static BenefitResDTO.FavoriteBenefitStatusDTO toFavoriteBenefitStatusDTO(Long benefitId, Boolean favorite) {
      return BenefitResDTO.FavoriteBenefitStatusDTO.builder()
          .benefitId(benefitId)
          .isFavorite(favorite)
          .build();
   }

   public static List<BenefitResDTO.DeadlineSoonBenefitDTO> toDeadlineSoonBenefitList(
       List<WelfareBenefit> benefits, LocalDate today) {
      return benefits.stream()
          .map(benefit -> toDeadlineSoonBenefitDTO(benefit, today))
          .toList();
   }

   public static BenefitResDTO.DeadlineSoonBenefitDTO toDeadlineSoonBenefitDTO(
       WelfareBenefit benefit, LocalDate today) {

      LocalDate endDate = benefit.getApplicationEndDate();

      return BenefitResDTO.DeadlineSoonBenefitDTO.builder()
          .benefitId(benefit.getId())
          .title(benefit.getTitle())
          .applicationEndDate(endDate)
          .dDay(endDate == null ? null : (int) ChronoUnit.DAYS.between(today, endDate))
          .build();
   }

   public static BenefitResDTO.BenefitShareDTO toBenefitShareDTO(WelfareBenefit benefit){
       return BenefitResDTO.BenefitShareDTO.builder()
               .title(benefit.getTitle())
               .summary(benefit.getEasySummary())
               .build();
   }
}
