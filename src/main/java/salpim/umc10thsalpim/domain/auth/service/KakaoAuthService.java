package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.auth.client.KakaoOAuthClient;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.KakaoOAuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final SignupValidationService signupValidationService;

    @Transactional
    public AuthResDTO.KakaoLoginResult login(String authorizationCode) {
        KakaoOAuthResDTO.Token kakaoToken = kakaoOAuthClient.requestToken(authorizationCode);
        KakaoOAuthResDTO.UserInfo kakaoUserInfo = kakaoOAuthClient.requestUserInfo(kakaoToken.accessToken());

        if (kakaoUserInfo.id() == null) {
            throw new AuthException(AuthErrorCode.KAKAO_USER_ID_MISSING);
        }

        String kakaoId = String.valueOf(kakaoUserInfo.id());
        return memberRepository.findByLoginTypeAndKakaoId(SocialProvider.KAKAO, kakaoId)
                .map(member -> tokenService.issueKakaoLoginCompleteTokens(member))
                .orElseGet(() -> tokenService.issueSignupRequiredToken(SocialProvider.KAKAO, kakaoId));
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

        String normalizedPhoneNumber = signupValidationService.normalizePhoneNumber(request.phoneNumber());
        signupValidationService.validateDuplicatePhoneNumber(normalizedPhoneNumber);
        Region region = signupValidationService.findLeafRegion(request.regionId());

        memberRepository.save(MemberConverter.toKakaoMember(request, normalizedPhoneNumber, kakaoId, region));
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
