package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.client.KakaoOAuthClient;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    private static final String SIGNUP_TOKEN = "signup-token";
    private static final String KAKAO_ID = "kakao-123";
    private static final Long REGION_ID = 1L;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private TokenService tokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SignupValidationService signupValidationService;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Test
    void signupSavesKakaoMemberWithAdministrativeArea() {
        AuthReqDTO.KakaoSignup request = validRequest();
        Region administrativeArea = Region.create(null, "Hwajeong-dong", RegionLevel.ADMINISTRATIVE_AREA);

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims());
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID)).thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber())).thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID)).thenReturn(administrativeArea);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getLoginType()).isEqualTo(SocialProvider.KAKAO);
        assertThat(memberCaptor.getValue().getRegion()).isSameAs(administrativeArea);
    }

    @Test
    void signupFailsWhenRegionIsGeneralGu() {
        AuthReqDTO.KakaoSignup request = validRequest();

        when(tokenService.parseSignupToken(SIGNUP_TOKEN)).thenReturn(signupClaims());
        when(memberRepository.existsByLoginTypeAndKakaoId(SocialProvider.KAKAO, KAKAO_ID)).thenReturn(false);
        when(signupValidationService.normalizePhoneNumber(request.phoneNumber())).thenReturn("01012345678");
        when(signupValidationService.findLeafRegion(REGION_ID))
                .thenThrow(new RegionException(RegionErrorCode.REGION_NOT_LEAF));

        assertThatThrownBy(() -> kakaoAuthService.signup("Bearer " + SIGNUP_TOKEN, request))
                .isInstanceOfSatisfying(RegionException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_LEAF));

        verify(memberRepository, never()).save(any(Member.class));
    }

    private TokenDTO.SignupTokenClaims signupClaims() {
        return new TokenDTO.SignupTokenClaims(TokenPurpose.SIGNUP, SocialProvider.KAKAO, KAKAO_ID);
    }

    private AuthReqDTO.KakaoSignup validRequest() {
        return new AuthReqDTO.KakaoSignup(
                "Jihong",
                LocalDate.of(2002, 3, 11),
                Gender.MALE,
                "010-1234-5678",
                "Goyang-si Deogyang-gu Hwarang-ro 28",
                "B",
                37.1234567,
                126.1234567,
                REGION_ID
        );
    }
}
