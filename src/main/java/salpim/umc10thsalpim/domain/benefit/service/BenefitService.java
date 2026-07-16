package salpim.umc10thsalpim.domain.benefit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.converter.BenefitConverter;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.BenefitRule;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.repository.BenefitRuleRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final WelfareBenefitRepository welfareBenefitRepository;
    private final BenefitRuleRepository benefitRuleRepository;

    @Transactional(readOnly = true)
    public BenefitResDTO.GetApplicationHelperInfo getApplicationHelperInfo(Long welfareBenefitId){
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

        return BenefitConverter.toGetApplicationHelperInfo(
                welfareBenefit, isOnlineApplicationAvailable, applicationTypeList);
    }
}
