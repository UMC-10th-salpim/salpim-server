package salpim.umc10thsalpim.domain.benefit.converter;

import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

public class BenefitConverter {

    public static CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> toPagination(List<WelfareBenefit> page, String nextCursor, Integer totalCount) {
        return CursorResDTO.Pagination.<BenefitResDTO.WelfareSearchResultDTO>builder()
                .data(page.stream()
                        .map(BenefitConverter::toWelfareSearchRes)
                        .toList())
                .hasNext(nextCursor!=null)
                .nextCursor(nextCursor)
                .totalCount(totalCount)
                .pageSize(page.size())
                .build();
    }

    public static BenefitResDTO.WelfareSearchResultDTO toWelfareSearchRes(WelfareBenefit banefit){
        return BenefitResDTO.WelfareSearchResultDTO.builder()
                .benefitId(banefit.getId())
                .benefitTitle(banefit.getTitle())
                .benefitCategory(banefit.getWelfareCategory().getName())
                .build();
    }
}
