package salpim.umc10thsalpim.domain.recommendation.service;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.recommendation.converter.RecommendationConverter;
import salpim.umc10thsalpim.domain.recommendation.dto.RecommendationResDTO;
import salpim.umc10thsalpim.domain.recommendation.entity.RecommendationOption;
import salpim.umc10thsalpim.domain.recommendation.exception.RecommendationException;
import salpim.umc10thsalpim.domain.recommendation.exception.code.RecommendationErrorCode;
import salpim.umc10thsalpim.domain.recommendation.repository.RecommendationRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;

    private final BenefitService benefitService;
    private final RegionQueryService regionQueryService;

    public RecommendationResDTO.RecommendationOptionsDTO getRecommendationOptions(Long categoryId) {
        List<RecommendationOption>  recommendationOptions = recommendationRepository.findAllByCategoryIdOrderByOptionOrderAsc(categoryId);
        return RecommendationConverter.toRecommendationOptionRes(recommendationOptions);
    }

    public CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> getRecommendationResult(Long optionId, Long memberId, String cursor, @Positive Integer pageSize) {
        RecommendationOption recommendationOption = recommendationRepository.findById(optionId).orElseThrow(() ->
                new RecommendationException(RecommendationErrorCode.OPTION_ID_NOT_FOUND)
        );

        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
        );

        Region memberRegion = regionRepository.findById(member.getRegionId())
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        Region sido = regionQueryService.findAncestorRegionOrThrow(memberRegion, RegionLevel.SIDO);
        Region sigungu = regionQueryService.findAncestorRegionOrThrow(memberRegion, RegionLevel.SIGUNGU);
        List<Long> regionIds = List.of(sido.getId(), sigungu.getId());

        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(recommendationOption.getCategoryId());

        return benefitService.getSearchResult(recommendationOption.getSearchKey(), regionIds, categoryIds, cursor, pageSize, "popular");
    }
}
