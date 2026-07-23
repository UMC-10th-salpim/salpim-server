package salpim.umc10thsalpim.domain.benefit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.converter.BenefitConverter;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareCategory;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.repository.BenefitRuleRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareCategoryRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;
import salpim.umc10thsalpim.global.dto.CursorResDTO;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final BokjiroApiClient bokjiroApiClient;

    private static final int API_MAX_SIZE=500;
    private static final int MAX_SERV_NUMBER=2000;
    private static final String SOURCE_NATIONAL = "NATIONAL";
    private static final String SOURCE_LOCAL = "LOCAL";

    private final WelfareBenefitRepository welfareBenefitRepository;
    private final BenefitRuleRepository benefitRuleRepository;
    private final RegionRepository regionRepository;
    private final MemberRepository memberRepository;
    private final WelfareCategoryRepository welfareCategoryRepository;

    @Transactional(readOnly = true)
    public BenefitResDTO.GetApplicationHelperInfo getApplicationHelperInfo(
            Long memberId,
            Long welfareBenefitId){
        WelfareBenefit welfareBenefit = welfareBenefitRepository.findById(welfareBenefitId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.BENEFIT_NOT_FOUND));

        List<BenefitRule> benefitRules = getBenefitRulesOrThrow(welfareBenefitId);

        List<ApplicationType> applicationTypeList = benefitRules.stream()
                .map(BenefitRule::getApplicationType)
                .distinct()
                .toList();

        Boolean isOnlineApplicationAvailable = isOnlineApplicationAvailable(benefitRules);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Region memberRegion = regionRepository.findById(member.getRegionId())
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        Boolean isRegionSatisfied = isRegionSatisfied(
                memberRegion,
                welfareBenefit.getRegionId(),
                welfareBenefit.getRegionScope()
        );

        Boolean isAgeSatisfied = isAgeSatisfied(member, welfareBenefit);

        return BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit, isOnlineApplicationAvailable,
                applicationTypeList, isRegionSatisfied, isAgeSatisfied);
    }

    @Transactional(readOnly = true)
    public String getOnlineApplicationUrl(Long welfareBenefitId) {
        WelfareBenefit welfareBenefit = welfareBenefitRepository.findById(welfareBenefitId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.BENEFIT_NOT_FOUND));

        List<BenefitRule> benefitRules = getBenefitRulesOrThrow(welfareBenefitId);

        boolean isOnlineApplicationAvailable = isOnlineApplicationAvailable(benefitRules);

        if(!isOnlineApplicationAvailable){
            throw new BenefitException(BenefitErrorCode.BENEFIT_ONLINE_APPLICATION_NOT_AVAILABLE);
        }

        return validateApplicationUrl(welfareBenefit.getApplicationUrl());
    }


    private Boolean isRegionSatisfied(
            Region memberRegion,
            Long benefitRegionId,
            RegionScope regionScope
    ) {
        if(regionScope == RegionScope.NONE){
            return true;
        }

        if(benefitRegionId == null){
            throw new BenefitException(
                    BenefitErrorCode.BENEFIT_REGION_NOT_CONFIGURED
            );
        }

        RegionLevel targetLevel = switch(regionScope){
            case MEMBER_DONG -> RegionLevel.DONG;
            case MEMBER_SIGUNGU -> RegionLevel.SIGUNGU;
            case MEMBER_SIDO -> RegionLevel.SIDO;
            case NONE -> throw new IllegalStateException(
                    "NONE scope is handled before region level mapping."
            );
        };

        Region benefitRegion = regionRepository.findById(benefitRegionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        if (benefitRegion.getRegionLevel() != targetLevel){
            throw new BenefitException(
                    BenefitErrorCode.BENEFIT_REGION_LEVEL_MISMATCH
            );
        }

        Region memberRegionAtTargetLevel = findAncestorRegion(memberRegion, targetLevel);

        return memberRegionAtTargetLevel != null && memberRegionAtTargetLevel.getId().equals(benefitRegionId);
    }

    private Region findAncestorRegion(Region region, RegionLevel targetLevel){
        Region current = region;

        while(current != null){
            if(current.getRegionLevel() == targetLevel){
                return current;
            }

            if(current.getParentId() == null){
                return null;
            }

            current = regionRepository.findById(current.getParentId())
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        }

        return null;
    }

    private Boolean isAgeSatisfied(Member member, WelfareBenefit welfareBenefit) {
        return switch (welfareBenefit.getAgeConditionStatus()) {
            case UNKNOWN -> null;
            case NO_RESTRICTION -> true;
            case RESTRICTED -> {
                int age = Period.between(member.getBirthDate(), LocalDate.now()).getYears();

                boolean meetsMinAge = welfareBenefit.getMinAge() == null
                        || age >= welfareBenefit.getMinAge();

                boolean meetsMaxAge = welfareBenefit.getMaxAge() == null
                        || age <= welfareBenefit.getMaxAge();

                yield meetsMinAge && meetsMaxAge;
            }
        };
    }

    private String validateApplicationUrl(String applicationUrl){
        if(applicationUrl == null || applicationUrl.isBlank()){
            throw new BenefitException(
                    BenefitErrorCode.BENEFIT_APPLICATION_URL_NOT_CONFIGURED
            );
        }

        try {
            URI uri = URI.create(applicationUrl);

            boolean isHttpUrl = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());

            if (!isHttpUrl || uri.getHost() == null) {
                throw new BenefitException(BenefitErrorCode.BENEFIT_APPLICATION_URL_INVALID);
            }

            return applicationUrl;
        }
        catch (IllegalArgumentException exception) {
            throw new BenefitException(
                    BenefitErrorCode.BENEFIT_APPLICATION_URL_INVALID
            );
        }
    }

    private List<BenefitRule> getBenefitRulesOrThrow(Long welfareBenefitId) {
        List<BenefitRule> benefitRules = benefitRuleRepository.findAllByWelfareBenefitId(welfareBenefitId);

        if(benefitRules.isEmpty()) {
            throw new BenefitException(BenefitErrorCode.BENEFIT_RULE_NOT_FOUND);
        }

        return benefitRules;
    }

    private boolean isOnlineApplicationAvailable(List<BenefitRule> benefitRules) {
        return benefitRules.stream()
                .anyMatch(benefitRule -> benefitRule.getApplicationType() == ApplicationType.ONLINE);
    }

    @Transactional(readOnly = true)
    public CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> getSearchResult(String searchKey, List<Long> regionIds, List<Long> categoryIds, String cursor, Integer pageSize, String sort) {

        String nextCursor;
        Integer totalCount;

        //지자체 복지 검색에 쓸 리스트
        List<Region> regions=regionRepository.findAllById(regionIds);

        //조회수를 담을 list
        Map<String, Integer> viewCountMap =  new HashMap<>();


        //복지로 api 이용해서 검색어에 맞는 혜택 id를 리스트에 넣기
        List<String> servIds_N = new ArrayList<>();
        List<String> servIds_L = new ArrayList<>();
        int pageNumber = 1;
        while(servIds_N.size()<MAX_SERV_NUMBER && servIds_L.size()<MAX_SERV_NUMBER){
            BokjiroApiDTO.BenefitListRes NationalRes =
                    bokjiroApiClient.searchNationalBenefits(pageNumber, API_MAX_SIZE, searchKey, null);

            BokjiroApiDTO.BenefitListRes LocalRes =
                    bokjiroApiClient.searchLocalBenefits(pageNumber, API_MAX_SIZE, searchKey, null, regions.get(0).getName(), regions.get(1).getName());

            NationalRes.getBenefitList().forEach(item -> {servIds_N.add(item.getServId());
                viewCountMap.put(SOURCE_NATIONAL+":"+item.getServId(), Integer.parseInt(item.getInqNum()));
            });
            LocalRes.getBenefitList().forEach(item -> {servIds_L.add(item.getServId());
                viewCountMap.put(SOURCE_LOCAL+":"+item.getServId(), Integer.parseInt(item.getInqNum()));
            });

            if (pageNumber*API_MAX_SIZE>=NationalRes.getTotalCount()&&
            pageNumber*API_MAX_SIZE>=LocalRes.getTotalCount()){ break; }
            pageNumber++;
        }

        //DB 매칭 & 카테고리 필터링
        List<WelfareBenefit> matched = new ArrayList<>();
        if (!servIds_N.isEmpty()) {
            matched.addAll(welfareBenefitRepository.findByExternalIdInAndSource(servIds_N, SOURCE_NATIONAL));
        }
        if (!servIds_L.isEmpty()) {
            matched.addAll(welfareBenefitRepository.findByExternalIdInAndSource(servIds_L, SOURCE_LOCAL));
        }

        List<WelfareBenefit> filtered = matched.stream()
                .filter(b -> categoryIds==null || categoryIds.isEmpty()||
                        categoryIds.contains(b.getCategoryId()))
                .toList();

        totalCount=filtered.size();

        //정렬
        List<WelfareBenefit> sortedBenefits = sortBenefits(filtered, sort, viewCountMap);

        //페이징
        List<WelfareBenefit> afterCursor = applyCursor(sortedBenefits, cursor);
        List<WelfareBenefit> page = afterCursor.stream().limit(pageSize).toList();
        boolean hasNext = afterCursor.size()>pageSize;
        nextCursor = hasNext ? afterCursor.get(pageSize).getId().toString() : null;

        List<Long> pageCategoryIds = page.stream()
                .map(WelfareBenefit::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> categoryNameMap = welfareCategoryRepository.findAllById(pageCategoryIds).stream()
                .collect(Collectors.toMap(WelfareCategory::getId, WelfareCategory::getName));

        return BenefitConverter.toPagination(page, nextCursor, totalCount, categoryNameMap);
    }

    private List<WelfareBenefit> sortBenefits(List<WelfareBenefit> benefits,
                                              String sort, Map<String, Integer> viewCountMap) {
        Comparator<WelfareBenefit> comparator = switch (sort.toLowerCase()) {
            case "popular" -> Comparator
                    .comparing((WelfareBenefit b) -> viewCountMap.getOrDefault(b.getSource() + ":" + b.getExternalId(), 0),
                            Comparator.reverseOrder())
                    .thenComparing(WelfareBenefit::getId); //동일하다면 id순 정렬
            case "deadline" -> Comparator
                    .comparing(WelfareBenefit::getApplicationEndDate,
                            Comparator.nullsLast(Comparator.naturalOrder())) // 마감 가까운 순, 비어있으면 뒤로
                    .thenComparing(WelfareBenefit::getId);
            default -> throw new ProjectException(BenefitErrorCode.INVALID_SORT_TYPE);
        };
        return benefits.stream().sorted(comparator).toList();
    }

    private List<WelfareBenefit> applyCursor(List<WelfareBenefit> benefits, String cursor) {
        if (cursor.equals("-1")) {
            return benefits;
        }else{
            for (int i=0; i<benefits.size(); i++) {
                if (benefits.get(i).getId().toString().equals(cursor)) {
                    return benefits.subList(i+1, benefits.size());
                }
            }
        }
        return Collections.emptyList();
    }
}
