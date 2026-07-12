package salpim.umc10thsalpim.domain.benefit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.converter.BenefitConverter;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.exception.BenefitException;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final WelfareBenefitRepository welfareBenefitRepository;

    @Transactional(readOnly = true)
    public BenefitResDTO.GetApplicationHelperInfo getApplicationHelperInfo(Long welfareBenefitId){
        WelfareBenefit welfareBenefit = welfareBenefitRepository.findById(welfareBenefitId)
                .orElseThrow(() -> new BenefitException(BenefitErrorCode.BENEFIT_NOT_FOUND));

        return BenefitConverter.toGetApplicationHelperInfo(welfareBenefit);
    }
}
