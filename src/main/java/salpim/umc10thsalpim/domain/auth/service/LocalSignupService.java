package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.exception.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalSignupService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(AuthReqDTO.LocalSignup request) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());

        validateLocalRequiredFields(request);
        validateDuplicatePhoneNumber(normalizedPhoneNumber);
        phoneVerificationService.validateVerifiedPhoneNumber(normalizedPhoneNumber);
        Region region = findRegion(request.regionId());

        String encodedPassword = passwordEncoder.encode(request.password());
        memberRepository.save(
                MemberConverter.toLocalMember(request, normalizedPhoneNumber, encodedPassword, region)
        );
        phoneVerificationService.deleteVerification(normalizedPhoneNumber);
    }

    private void validateLocalRequiredFields(AuthReqDTO.LocalSignup request) {
        if (!StringUtils.hasText(request.password())) {
            throw new MemberException(MemberErrorCode.REQUIRED_LOCAL_PASSWORD);
        }
        if (!StringUtils.hasText(request.passwordAnswer())) {
            throw new MemberException(MemberErrorCode.REQUIRED_PASSWORD_RECOVERY_ANSWER);
        }
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }

    private Region findRegion(Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        if (region.getRegionLevel() != RegionLevel.EUP_MYEON_DONG) {
            throw new RegionException(RegionErrorCode.REGION_NOT_LEAF);
        }
        return region;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
