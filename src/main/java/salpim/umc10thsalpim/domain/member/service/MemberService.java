package salpim.umc10thsalpim.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.PasswordVerificationAttemptService;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.enums.WordSize;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;

    private final PasswordVerificationAttemptService passwordVerificationAttemptService;
    private final RegionQueryService regionQueryService;
    private final TokenService tokenService;
    private final PasswordPolicy passwordPolicy;

    @Transactional(readOnly = true)
    public MemberResDTO.MyPageInfo getMyPage(Long memberId) {
        Member member = getMemberOrThrow(memberId);

        if(member.getRegionId() == null) {
            throw new MemberException(MemberErrorCode.MEMBER_REGION_NOT_SET);
        }

        Region memberRegion = getRegionOrThrow(member.getRegionId());

        Region sido = regionQueryService.findAncestorRegionOrThrow(memberRegion, RegionLevel.SIDO);
        Region sigungu = regionQueryService.findAncestorRegionOrThrow(memberRegion, RegionLevel.SIGUNGU);
        Region generalGu = regionQueryService.findAncestorRegion(memberRegion, RegionLevel.GENERAL_GU);

        return MemberConverter.toMyPageInfo(
                member,
                memberRegion.getId(),
                sido.getName(),
                sigungu.getName(),
                generalGu != null ? generalGu.getName() : null,
                memberRegion.getName()
        );
    }

    @Transactional(readOnly = true)
    public MemberResDTO.WelfareCenterInfo getWelfareCenter(Long memberId) {
        Member member = getMemberOrThrow(memberId);

        return MemberConverter.toWelfareCenterInfo(member);
    }

    @Transactional
    public void updateProfile(Long memberId, MemberReqDTO.UpdateProfile request) {
        Member member = getMemberOrThrow(memberId);

        Region region = getRegionOrThrow(request.regionId());

        if (region.getRegionLevel() != RegionLevel.ADMINISTRATIVE_AREA) {
            throw new RegionException(RegionErrorCode.REGION_LEVEL_INVALID);
        }

        updatePhoneNumberIfRequested(member, request);

        String welfareCenter = region.getName();

        member.updateProfile(
                request.name().trim(),
                request.birthDate(),
                request.gender(),
                request.roadAddress().trim(),
                MemberConverter.normalizeNullableText(request.detailAddress()),
                request.latitude(),
                request.longitude(),
                region,
                welfareCenter
        );
    }

    @Transactional
    public void updateWordSize(Long memberId, WordSize wordSize) {
        Member member = getMemberOrThrow(memberId);

        member.updateWordSize(wordSize);
    }

    @Transactional(readOnly = true)
    public MemberResDTO.PasswordVerificationResult verifyCurrentPassword(
            Long memberId,
            MemberReqDTO.VerifyCurrentPassword request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validatePasswordChangeAttemptAllowed(memberId);

        try {
            validateCurrentPassword(member, request.currentPassword());
        } catch (MemberException e) {
            recordPasswordChangeFailure(memberId);
            throw e;
        }

        clearPasswordChangeFailures(memberId);

        return new MemberResDTO.PasswordVerificationResult(true);
    }

    @Transactional(readOnly = true)
    public MemberResDTO.PasswordVerificationResult verifyRecoveryAnswer(
            Long memberId,
            MemberReqDTO.VerifyRecoveryAnswer request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validatePasswordChangeAttemptAllowed(memberId);

        try {
            validateRecoveryAnswer(member, request.recoveryAnswer());
        } catch (MemberException e) {
            recordPasswordChangeFailure(memberId);
            throw e;
        }

        clearPasswordChangeFailures(memberId);

        return new MemberResDTO.PasswordVerificationResult(true);
    }

    @Transactional
    public void changePassword(
            Long memberId,
            MemberReqDTO.ChangePassword request
    ) {
        Member member = getLocalMemberOrThrow(memberId);

        validatePasswordChangeAttemptAllowed(memberId);

        try {
            validatePasswordVerification(member, request);
        } catch (MemberException e) {
            if (isPasswordVerificationFailure(e)) {
                recordPasswordChangeFailure(memberId);
            }

            throw e;
        }

        passwordPolicy.validateNewPasswordIsDifferent(member, request.newPassword());

        clearPasswordChangeFailures(memberId);
        member.changePassword(passwordEncoder.encode(request.newPassword()));
        tokenService.invalidateMemberSession(member);
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
                recoveryAnswer.trim(),
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

    private void validatePasswordChangeAttemptAllowed(Long memberId) {
        passwordVerificationAttemptService.validateAttemptAllowed(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                memberId.toString()
        );
    }

    private void recordPasswordChangeFailure(Long memberId) {
        passwordVerificationAttemptService.recordFailure(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                memberId.toString()
        );
    }

    private void clearPasswordChangeFailures(Long memberId) {
        passwordVerificationAttemptService.clearFailures(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                memberId.toString()
        );
    }

    private boolean isPasswordVerificationFailure(MemberException e) {
        return e.getErrorCode() == MemberErrorCode.PASSWORD_MISMATCH
                || e.getErrorCode() == MemberErrorCode.RECOVERY_ANSWER_MISMATCH;
    }

}
