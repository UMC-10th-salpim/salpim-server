package salpim.umc10thsalpim.domain.recommendation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.recommendation.entity.RecommendationOption;
import salpim.umc10thsalpim.domain.recommendation.repository.RecommendationRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private BenefitService benefitService;

    @Mock
    private RegionQueryService regionQueryService;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void usesSidoAndSigunguForMemberUnderGeneralGu() {
        RecommendationOption option = RecommendationOption.builder()
                .id(1L)
                .searchKey("senior")
                .categoryId(10L)
                .build();
        Member member = Member.builder().id(2L).regionId(4L).build();
        Region administrativeArea = region(4L, 3L, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);
        Region sido = region(1L, null, "Gyeonggi-do", RegionLevel.SIDO);
        Region sigungu = region(2L, 1L, "Goyang-si", RegionLevel.SIGUNGU);
        CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> expected =
                new CursorResDTO.Pagination<>(List.of(), false, null, 0, 0);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(option));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(regionRepository.findById(4L)).thenReturn(Optional.of(administrativeArea));
        when(regionQueryService.findAncestorRegionOrThrow(administrativeArea, RegionLevel.SIDO))
                .thenReturn(sido);
        when(regionQueryService.findAncestorRegionOrThrow(administrativeArea, RegionLevel.SIGUNGU))
                .thenReturn(sigungu);
        when(benefitService.getSearchResult(any(), any(), any(), any(), any(), any()))
                .thenReturn(expected);

        CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> result =
                recommendationService.getRecommendationResult(1L, 2L, "cursor", 10);

        assertThat(result).isSameAs(expected);
        verify(benefitService).getSearchResult(
                "senior",
                List.of(1L, 2L),
                List.of(10L),
                "cursor",
                10,
                "popular"
        );
    }

    private Region region(Long id, Long parentId, String name, RegionLevel level) {
        return Region.builder()
                .id(id)
                .parentId(parentId)
                .name(name)
                .regionLevel(level)
                .build();
    }
}
