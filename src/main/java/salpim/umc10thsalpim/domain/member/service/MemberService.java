package salpim.umc10thsalpim.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MemberResDTO.MyPageInfo getMyPage(Long memberId) {
        Member member = getMemberOrThrow(memberId);

        if(member.getRegionId() == null) {
            throw new MemberException(MemberErrorCode.MEMBER_REGION_NOT_SET);
        }

        Region memberRegion = getRegionOrThrow(member.getRegionId());

        Region sido = findAncestorRegion(memberRegion, RegionLevel.SIDO);
        Region sigungu = findAncestorRegion(memberRegion, RegionLevel.SIGUNGU);

        return MemberConverter.toMyPageInfo(
                member,
                sido.getName(),
                sigungu.getName()
        );
    }

    @Transactional
    public void updateProfile(Long memberId, MemberReqDTO.UpdateProfile request) {
        Member member = getMemberOrThrow(memberId);

        Region region = getRegionOrThrow(request.regionId());

        if (region.getRegionLevel() != RegionLevel.DONG) {
            throw new RegionException(RegionErrorCode.REGION_LEVEL_INVALID);
        }

        updatePhoneNumberIfRequested(member, request);

        member.updateProfile(
                request.name().trim(),
                request.birthDate(),
                request.gender(),
                request.roadAddress().trim(),
                MemberConverter.normalizeNullableText(request.detailAddress()),
                request.latitude(),
                request.longitude(),
                region
        );
    }

    @Transactional(readOnly = true)
    public MemberResDTO.PasswordVerificationResult verifyCurrentPassword(
            Long memberId,
            MemberReqDTO.VerifyCurrentPassword request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validateCurrentPassword(member, request.currentPassword());

        return new MemberResDTO.PasswordVerificationResult(true);
    }

    @Transactional(readOnly = true)
    public MemberResDTO.PasswordVerificationResult verifyRecoveryAnswer(
            Long memberId,
            MemberReqDTO.VerifyRecoveryAnswer request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validateRecoveryAnswer(member, request.recoveryAnswer());

        return new MemberResDTO.PasswordVerificationResult(true);
    }

    @Transactional
    public void changePassword(
            Long memberId,
            MemberReqDTO.ChangePassword request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validatePasswordVerification(member, request);

        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    private Region findAncestorRegion(Region region, RegionLevel targetLevel) {
        Region currentRegion = region;

        while (currentRegion.getRegionLevel() != targetLevel) {
            Long parentId = currentRegion.getParentId();

            if(parentId == null){
                throw new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID);
            }

            currentRegion = getRegionOrThrow(parentId);
        }

        return currentRegion;
    }

    private void updatePhoneNumberIfRequested(
            Member member,
            MemberReqDTO.UpdateProfile request
    ) {
        boolean hasPhoneNumber = StringUtils.hasText(request.phoneNumber());
        boolean hasVerificationToken = StringUtils.hasText(request.phoneVerificationToken());

        if (!hasPhoneNumber && !hasVerificationToken) {
            return;
        }

        if (!hasPhoneNumber || !hasVerificationToken) {
            throw new AuthException(AuthErrorCode.PHONE_CHANGE_REQUEST_INVALID);
        }

        String normalizedPhoneNumber = phoneVerificationService
                .validateAndConsumePhoneChangeToken(
                        member,
                        request.phoneNumber(),
                        request.phoneVerificationToken()
                );

        if (memberRepository.existsByPhoneNumberAndIdNot(
                normalizedPhoneNumber,
                member.getId()
        )) {
            throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        member.updatePhoneNumber(normalizedPhoneNumber);
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Region getRegionOrThrow(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
    }

    private Member getLocalMemberOrThrow(Long memberId) {
        Member member = getMemberOrThrow(memberId);

        if(member.getLoginType() != SocialProvider.LOCAL){
            throw new MemberException(MemberErrorCode.PASSWORD_CHANGE_NOT_SUPPORTED);
        }

        return member;
    }

    private void validateCurrentPassword(Member member, String currentPassword) {
        if(!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new MemberException(MemberErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateRecoveryAnswer(Member member, String recoveryAnswer) {
        if (!passwordEncoder.matches(
                recoveryAnswer,
                member.getPasswordRecoveryAnswer()
        )) {
            throw new MemberException(MemberErrorCode.RECOVERY_ANSWER_MISMATCH);
        }
    }

    private void validatePasswordVerification(
            Member member, MemberReqDTO.ChangePassword request
    ) {
        if (request.verificationMethod() == null) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD_VERIFICATION);
        }

        boolean hasCurrentPassword = StringUtils.hasText(request.currentPassword());
        boolean hasRecoveryAnswer = StringUtils.hasText(request.recoveryAnswer());

        switch (request.verificationMethod()) {
            case CURRENT_PASSWORD -> {
                if (!hasCurrentPassword || hasRecoveryAnswer) {
                    throw new MemberException(MemberErrorCode.INVALID_PASSWORD_VERIFICATION);
                }

                validateCurrentPassword(member, request.currentPassword());
            }
            case RECOVERY_ANSWER -> {
                if (!hasRecoveryAnswer || hasCurrentPassword) {
                    throw new MemberException(MemberErrorCode.INVALID_PASSWORD_VERIFICATION);
                }

                validateRecoveryAnswer(member, request.recoveryAnswer());
            }


        }

    }

}
