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
import salpim.umc10thsalpim.domain.auth.security.JwtAuthenticationFilter;
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
                        "지원 대상",
                        "주민센터 방문",
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
        given(tokenService.validateAccessTokenAndGetMemberId("access-token"))
                .willReturn(1L);
        given(memberRepository.existsById(1L)).willReturn(true);

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
}
