package salpim.umc10thsalpim.domain.benefit.converter;

import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.global.dto.CursorResDTO;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;

import java.util.List;
import java.util.Map;

public class BenefitConverter {

    public static CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> toPagination(List<WelfareBenefit> page, String nextCursor, Integer totalCount, Map<Long, String> categoryNameMap) {
        return CursorResDTO.Pagination.<BenefitResDTO.WelfareSearchResultDTO>builder()
                .data(page.stream()
                        .map(b -> toWelfareSearchRes(b,
                                categoryNameMap.get(b.getCategoryId())))
                        .toList())
                .hasNext(nextCursor!=null)
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
            String categoryName
    ) {
        return new BenefitResDTO.GetBenefitDetailDTO(
                welfareBenefit.getTitle(),
                welfareBenefit.getEasySummary(),
                welfareBenefit.getWhoCanReceive(),
                welfareBenefit.getWhatYouReceive(),
                welfareBenefit.getRecommendedFor(),
                welfareBenefit.getApplicationStartDate(),
                welfareBenefit.getApplicationEndDate(),
                welfareBenefit.getApplicationUrl(),
                categoryName,
                welfareBenefit.getMinAge(),
                welfareBenefit.getMaxAge(),
                welfareBenefit.getAgeConditionStatus()
        );
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
}
