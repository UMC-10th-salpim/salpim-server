package salpim.umc10thsalpim.domain.benefit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.auth.service.AuthSecretHasher;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.security.JwtAuthenticationFilter;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.global.apiPayload.handler.GeneralExceptionAdvice;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BenefitController.class)
@Import(JwtAuthenticationFilter.class)
class BenefitControllerTest {

    @Autowired
    private BenefitController benefitController;

    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private BenefitService benefitService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AuthSecretHasher authSecretHasher;

    @MockitoBean
    private MemberRepository memberRepository;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(benefitController)
                .setControllerAdvice(new GeneralExceptionAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(jwtAuthenticationFilter)
                .build();
    }

    @Test
    @DisplayName("신청 도우미 정보 정상 조회")
    void returnsApplicationHelperInfo() throws Exception {
        Long benefitId = 100L;

        BenefitResDTO.GetApplicationHelperInfo response =
                new BenefitResDTO.GetApplicationHelperInfo(
                        benefitId,
                        "테스트 혜택",
                        "https://example.com",
                        "129",
                        "테스트 기관",
                        true,
                        List.of(ApplicationType.ONLINE),
                        LocalDate.of(2026, 12, 31),
                        true,
                        AgeConditionStatus.RESTRICTED,
                        65,
                        100,
                        true
                );

        given(benefitService.getApplicationHelperInfo(1L, benefitId))
                .willReturn(response);
        Member member = Member.builder()
                .id(1L)
                .loginType(SocialProvider.LOCAL)
                .password("encoded-password")
                .build();
        TokenDTO.AccessTokenClaims claims = new TokenDTO.AccessTokenClaims(
                TokenPurpose.ACCESS,
                1L,
                "credential-fingerprint"
        );

        given(tokenService.parseAccessToken("access-token")).willReturn(claims);
        given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member));
        given(authSecretHasher.createCredentialFingerprint(
                SocialProvider.LOCAL,
                "encoded-password"
        )).willReturn("expected-fingerprint");
        given(authSecretHasher.matchesCredentialFingerprint(
                "credential-fingerprint",
                "expected-fingerprint"
        )).willReturn(true);

        mockMvc.perform(get("/api/benefits/{benefitId}/application-helper", benefitId)
                        .header("Authorization", "Bearer access-token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("BENEFIT200_1"))
                .andExpect(jsonPath("$.result.benefitId").value(100))
                .andExpect(jsonPath("$.result.isRegionSatisfied").value(true))
                .andExpect(jsonPath("$.result.ageConditionStatus").value("RESTRICTED"))
                .andExpect(jsonPath("$.result.minAge").value(65))
                .andExpect(jsonPath("$.result.maxAge").value(100))
                .andExpect(jsonPath("$.result.isAgeSatisfied").value(true));

        verify(benefitService).getApplicationHelperInfo(1L, benefitId);
    }

    @Test
    @DisplayName("온라인 신청 사이트로 정상 리다이렉트")
    void redirectsToOnlineApplicationSite() throws Exception {
        Long benefitId = 100L;
        String applicationUrl = "https://example.com/apply";

        given(benefitService.getOnlineApplicationUrl(benefitId))
                .willReturn(applicationUrl);

        mockMvc.perform(get("/api/benefits/{benefitId}/application-link", benefitId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", applicationUrl));

        verify(benefitService).getOnlineApplicationUrl(benefitId);
    }

    @Test
    @DisplayName("카카오톡 공유하기용 혜택 정보 정상 조회 API")
    void returnsBenefitShareInfo() throws Exception {
        Long benefitId = 100L;
        BenefitResDTO.BenefitShareDTO response = BenefitResDTO.BenefitShareDTO.builder()
                .title("청년월세 특별지원")
                .summary("청년 가구에 월세를 지원합니다.")
                .build();

        given(benefitService.getBenefitShareInfo(benefitId))
                .willReturn(response);

        mockMvc.perform(get("/api/benefits/{benefitId}/share", benefitId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SHARE200_1"))
                .andExpect(jsonPath("$.result.title").value("청년월세 특별지원"))
                .andExpect(jsonPath("$.result.summary").value("청년 가구에 월세를 지원합니다."));

        verify(benefitService).getBenefitShareInfo(benefitId);
    }
}
