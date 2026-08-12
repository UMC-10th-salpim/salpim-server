package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import salpim.umc10thsalpim.domain.auth.client.KakaoOAuthClient;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.KakaoOAuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.enums.NextStep;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.enums.WordSize;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    private static final String SIGNUP_TOKEN = "signup-token";
    private static final String KAKAO_ID = "123";
    private static final Long REGION_ID = 1L;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private TokenService tokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SignupValidationService signupValidationService;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @Mock
    private TermsAgreementVerificationService termsAgreementVerificationService;

    @Mock
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Test
    void loginRequiresPhoneVerificationWhenKakaoPhoneNumberIsMissing() {
        KakaoOAuthResDTO.Token kakaoToken = kakaoToken();
        KakaoOAuthResDTO.UserInfo userInfo = kakaoUserInfo(null, true);
        AuthResDTO.KakaoLoginResult expected = AuthResDTO.KakaoLoginResult.builder()
                .isNewMember(true)
                .nextStep(NextStep.SIGNUP_REQUIRED)
                .signupToken("signup-token")
                .phoneVerificationRequired(true)
                .build();

        when(kakaoOAuthClient.requestToken("authorization-code")).thenReturn(kakaoToken);
        when(kakaoOAuthClient.requestUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(memberRepository.findByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(Optional.empty());
        when(tokenService.issueSignupRequiredToken(SocialProvider.KAKAO, KAKAO_ID, null))
                .thenReturn(expected);

        AuthResDTO.KakaoLoginResult result = kakaoAuthService.login("authorization-code");

        assertThat(result.phoneNumber()).isNull();
        assertThat(result.phoneVerificationRequired()).isTrue();
    }

    @Test
    void loginUsesNormalizedPhoneNumberWhenKakaoProvidesIt() {
        KakaoOAuthResDTO.Token kakaoToken = kakaoToken();
        KakaoOAuthResDTO.UserInfo userInfo = kakaoUserInfo("+82 10-1234-5678", false);
        AuthResDTO.KakaoLoginResult expected = AuthResDTO.KakaoLoginResult.builder()
                .isNewMember(true)
                .nextStep(NextStep.SIGNUP_REQUIRED)
                .signupToken("signup-token")
                .phoneNumber("01012345678")
                .phoneVerificationRequired(false)
                .build();

        when(kakaoOAuthClient.requestToken("authorization-code")).thenReturn(kakaoToken);
        when(kakaoOAuthClient.requestUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(memberRepository.findByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(Optional.empty());
        when(tokenService.issueSignupRequiredToken(
                SocialProvider.KAKAO,
                KAKAO_ID,
                "01012345678"
        )).thenReturn(expected);

        AuthResDTO.KakaoLoginResult result = kakaoAuthService.login("authorization-code");

        assertThat(result.phoneNumber()).isEqualTo("01012345678");
        assertThat(result.phoneVerificationRequired()).isFalse();
    }

    @Test
    void signupSavesKakaoMemberWithAdministrativeArea() {
        AuthReqDTO.KakaoSignup request = validRequest();
        Region administrativeArea = Region.create(null, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims(null));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID)).thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber())).thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID)).thenReturn(administrativeArea);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getLoginType()).isEqualTo(SocialProvider.KAKAO);
        assertThat(memberCaptor.getValue().getRegion()).isSameAs(administrativeArea);
        assertThat(memberCaptor.getValue().getWordSize()).isEqualTo(WordSize.LARGE);
        verify(phoneVerificationService).validateVerifiedPhoneNumber("01012345678");
        verify(phoneVerificationService).deleteVerification("01012345678");
    }

    @Test
    void signupFailsWhenRegionIsGeneralGu() {
        AuthReqDTO.KakaoSignup request = validRequest();

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims(null));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID)).thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber())).thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID))
                .thenThrow(new RegionException(RegionErrorCode.REGION_NOT_LEAF));

        assertThatThrownBy(() -> kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request))
                .isInstanceOfSatisfying(RegionException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_LEAF));

        verify(memberRepository, never()).saveAndFlush(any(Member.class));
    }

    @Test
    void signupUsesTrustedKakaoPhoneWithoutSmsVerification() {
        AuthReqDTO.KakaoSignup request = validRequest();
        Region administrativeArea = Region.create(
                null,
                "Hwajeong-dong",
                RegionLevel.ADMINISTRATIVE_AREA
        );

        when(tokenService.parseSignupToken(SIGNUP_TOKEN))
                .thenReturn(signupClaims("01099998888"));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(false);
        when(signupValidationService.findLeafRegion(REGION_ID)).thenReturn(administrativeArea);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getPhoneNumber()).isEqualTo("01099998888");
        verify(signupValidationService, never()).normalizePhoneNumber(any());
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void signupRejectsMissingPhoneWhenKakaoDidNotProvideIt() {
        AuthReqDTO.KakaoSignup request = requestWithPhone(null);

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims(null));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request))
                .isInstanceOfSatisfying(AuthException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AuthErrorCode.KAKAO_PHONE_VERIFICATION_REQUIRED));
        verify(memberRepository, never()).saveAndFlush(any(Member.class));
    }

    @Test
    void signupConvertsConcurrentPhoneConflictToDomainConflict() {
        AuthReqDTO.KakaoSignup request = validRequest();
        Region administrativeArea = Region.create(
                null,
                "Hwajeong-dong",
                RegionLevel.ADMINISTRATIVE_AREA
        );

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims(null));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber()))
                .thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID)).thenReturn(administrativeArea);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("uk_member_phone_number"));

        assertThatThrownBy(() -> kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.DUPLICATE_PHONE_NUMBER));
        verify(phoneVerificationService, never()).deleteVerification(any());
    }

    @Test
    void signupConvertsConcurrentKakaoAccountConflictToDomainConflict() {
        AuthReqDTO.KakaoSignup request = validRequest();
        Region administrativeArea = Region.create(
                null,
                "Hwajeong-dong",
                RegionLevel.ADMINISTRATIVE_AREA
        );

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims(null));
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID))
                .thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber()))
                .thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID)).thenReturn(administrativeArea);
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "uk_member_login_type_kakao_id"
                ));

        assertThatThrownBy(() -> kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.DUPLICATE_KAKAO_ACCOUNT));
        verify(phoneVerificationService, never()).deleteVerification(any());
    }

    private TokenDTO.SignupTokenClaims signupClaims(String providerPhoneNumber) {
        return new TokenDTO.SignupTokenClaims(
                TokenPurpose.SIGNUP,
                SocialProvider.KAKAO,
                KAKAO_ID,
                providerPhoneNumber
        );
    }

    private AuthReqDTO.KakaoSignup validRequest() {
        return requestWithPhone("010-1234-5678");
    }

    private AuthReqDTO.KakaoSignup requestWithPhone(String phoneNumber) {
        return new AuthReqDTO.KakaoSignup(
                "Jihong",
                LocalDate.of(2002, 3, 11),
                Gender.MALE,
                WordSize.LARGE,
                phoneNumber,
                "Goyang-si Deogyang-gu Hwarang-ro 28",
                "B",
                37.1234567,
                126.1234567,
                REGION_ID
        );
    }

    private KakaoOAuthResDTO.Token kakaoToken() {
        return new KakaoOAuthResDTO.Token(
                "kakao-access-token",
                "bearer",
                null,
                3600,
                null,
                null
        );
    }

    private KakaoOAuthResDTO.UserInfo kakaoUserInfo(
            String phoneNumber,
            Boolean phoneNumberNeedsAgreement
    ) {
        KakaoOAuthResDTO.KakaoAccount account = new KakaoOAuthResDTO.KakaoAccount(
                null,
                null,
                null,
                null,
                null,
                phoneNumber,
                phoneNumberNeedsAgreement,
                null
        );
        return new KakaoOAuthResDTO.UserInfo(Long.valueOf(KAKAO_ID), account);
    }
}
