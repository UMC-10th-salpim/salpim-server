package salpim.umc10thsalpim.domain.benefit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.converter.BenefitConverter;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.repository.BenefitRuleRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final WelfareBenefitRepository welfareBenefitRepository;
    private final BenefitRuleRepository benefitRuleRepository;
    private final RegionRepository regionRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public BenefitResDTO.GetApplicationHelperInfo getApplicationHelperInfo(
            Long memberId,
            Long welfareBenefitId){
        WelfareBenefit welfareBenefit = welfareBenefitRepository.findById(welfareBenefitId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.BENEFIT_NOT_FOUND));

        List<BenefitRule> benefitRuleList = benefitRuleRepository.findAllByWelfareBenefitId(welfareBenefitId);

        if(benefitRuleList.isEmpty()){
            throw new BenefitException(BenefitErrorCode.BENEFIT_RULE_NOT_FOUND);
        }

        List<ApplicationType> applicationTypeList = benefitRuleList.stream()
                .map(BenefitRule::getApplicationType)
                .distinct()
                .toList();

        Boolean isOnlineApplicationAvailable = benefitRuleList.stream()
                .anyMatch(benefitRule ->
                        benefitRule.getApplicationType() == ApplicationType.ONLINE);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Region memberRegion = regionRepository.findById(member.getRegionId())
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        Boolean isRegionSatisfied = benefitRuleList.stream()
                .anyMatch(rule -> isRegionSatisfied(
                        memberRegion,
                        welfareBenefit.getRegionId(),
                        rule.getRegionScope()
                ));

        return BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit, isOnlineApplicationAvailable, applicationTypeList, isRegionSatisfied);
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
            case USER_DONG -> RegionLevel.DONG;
            case USER_SIGUNGU -> RegionLevel.SIGUNGU;
            case USER_SI -> RegionLevel.SIDO;
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
}
