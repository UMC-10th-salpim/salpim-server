package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.client.KakaoOAuthClient;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.KakaoOAuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.entity.TermsAgreementVerificationItem;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.term.converter.TermConverter;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final SignupValidationService signupValidationService;
    private final PhoneVerificationService phoneVerificationService;
    private final TermsAgreementVerificationService termsAgreementVerificationService;
    private final MemberTermAgreementRepository memberTermAgreementRepository;

    @Transactional
    public AuthResDTO.KakaoLoginResult login(String authorizationCode) {
        KakaoOAuthResDTO.Token kakaoToken = kakaoOAuthClient.requestToken(authorizationCode);
        KakaoOAuthResDTO.UserInfo kakaoUserInfo = kakaoOAuthClient.requestUserInfo(kakaoToken.accessToken());

        if (kakaoUserInfo.id() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_USER_ID_MISSING);
        }

        String kakaoId = String.valueOf(kakaoUserInfo.id());
        String kakaoPhoneNumber = extractKakaoPhoneNumber(kakaoUserInfo.kakaoAccount());
        return memberRepository.findByLoginTypeAndKakaoId(SocialProvider.KAKAO, kakaoId)
                .map(member -> tokenService.issueKakaoLoginCompleteTokens(member))
                .orElseGet(() -> tokenService.issueSignupRequiredToken(
                        SocialProvider.KAKAO,
                        kakaoId,
                        kakaoPhoneNumber
                ));
    }

    @Transactional
    public void signup(String authorizationHeader, AuthReqDTO.KakaoSignup request) {
        String signupToken = extractBearerToken(authorizationHeader);
        TokenDTO.SignupTokenClaims signupTokenClaims = tokenService.parseSignupToken(signupToken);

        if (signupTokenClaims.provider() != SocialProvider.KAKAO) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_TYPE_INVALID);
        }

        String kakaoId = signupTokenClaims.providerId();
        if (memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, kakaoId)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_KAKAO_ACCOUNT);
        }

        boolean requiresPhoneVerification = !StringUtils.hasText(
                signupTokenClaims.providerPhoneNumber()
        );
        String normalizedPhoneNumber = resolveSignupPhoneNumber(
                signupTokenClaims,
                request.phoneNumber()
        );
        signupValidationService.validateDuplicatePhoneNumber(normalizedPhoneNumber);
        List<TermsAgreementVerificationItem> agreedTerms = List.of();
        if (requiresPhoneVerification) {
            phoneVerificationService.validateVerifiedPhoneNumber(normalizedPhoneNumber);
            agreedTerms = termsAgreementVerificationService.validateAgreedTerms(normalizedPhoneNumber);
        }
        Region region = signupValidationService.findLeafRegion(request.regionId());

        Member member;
        try {
            member = memberRepository.saveAndFlush(
                    MemberConverter.toKakaoMember(request, normalizedPhoneNumber, kakaoId, region)
            );
        } catch (DataIntegrityViolationException exception) {
            if (MemberConstraintViolationClassifier.isViolationOf(
                    exception,
                    MemberConstraintViolationClassifier.PHONE_NUMBER_CONSTRAINT
            )) {
                throw new MemberException(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            if (MemberConstraintViolationClassifier.isViolationOf(
                    exception,
                    MemberConstraintViolationClassifier.KAKAO_ACCOUNT_CONSTRAINT
            )) {
                throw new MemberException(MemberErrorCode.DUPLICATE_KAKAO_ACCOUNT);
            }
            throw exception;
        }
        agreedTerms.forEach(item -> memberTermAgreementRepository.save(
                TermConverter.toMemberAgreement(member, item.getTermsVersion(), item.getAgreed())
        ));
        if (requiresPhoneVerification) {
            phoneVerificationService.deleteVerification(normalizedPhoneNumber);
            termsAgreementVerificationService.invalidate(normalizedPhoneNumber);
        }
    }

    private String resolveSignupPhoneNumber(
            TokenDTO.SignupTokenClaims signupTokenClaims,
            String requestedPhoneNumber
    ) {
        if (StringUtils.hasText(signupTokenClaims.providerPhoneNumber())) {
            return signupTokenClaims.providerPhoneNumber(); // 카카오 계정에서 전화번호를 바로 받을 수 있는 경우 (사업자 등록 필요)
        }
        if (!StringUtils.hasText(requestedPhoneNumber)) {
            throw new AuthException(AuthErrorCode.KAKAO_PHONE_VERIFICATION_REQUIRED);
        }
        return signupValidationService.normalizePhoneNumber(requestedPhoneNumber);
    }

    private String extractKakaoPhoneNumber(KakaoOAuthResDTO.KakaoAccount kakaoAccount) {
        if (kakaoAccount == null || !StringUtils.hasText(kakaoAccount.phoneNumber())) {
            return null;
        }

        String digits = kakaoAccount.phoneNumber().replaceAll("[^0-9]", "");
        if (digits.startsWith("82")) {
            digits = "0" + digits.substring(2);
        }
        if (!digits.matches("^01[016789]\\d{7,8}$")) {
            return null;
        }
        return digits;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_INVALID);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new AuthException(AuthErrorCode.SIGNUP_TOKEN_INVALID);
        }
        return token;
    }
}
