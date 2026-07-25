package salpim.umc10thsalpim.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.member.service.MemberService;
import salpim.umc10thsalpim.domain.member.service.MemberWithdrawalService;
import salpim.umc10thsalpim.global.apiPayload.hander.GeneralExceptionAdvice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MemberController memberController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final Long MEMBER_ID = 1L;
    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberWithdrawalService memberWithdrawalService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                .setControllerAdvice(new GeneralExceptionAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(authenticatedMemberFilter())
                .build();
    }

    @Test
    @DisplayName("마이페이지 정보를 정상 조회한다")
    void getMyPageSuccess() throws Exception {
        MemberResDTO.MyPageInfo response =
                new MemberResDTO.MyPageInfo("홍길동", "인천광역시", "미추홀구");

        given(memberService.getMyPage(MEMBER_ID)).willReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_1"))
                .andExpect(jsonPath("$.result.name").value("홍길동"))
                .andExpect(jsonPath("$.result.sido").value("인천광역시"))
                .andExpect(jsonPath("$.result.sigungu").value("미추홀구"));

        verify(memberService).getMyPage(MEMBER_ID);
    }

    @Test
    @DisplayName("개인정보를 정상 수정한다")
    void updateProfileSuccess() throws Exception {
        MemberReqDTO.UpdateProfile request = createUpdateRequest();

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_2"));

        verify(memberService).updateProfile(MEMBER_ID, request);
    }

    @Test
    @DisplayName("이름이 없으면 개인정보 수정에 실패한다")
    void updateProfileFailsWhenNameIsBlank() throws Exception {
        MemberReqDTO.UpdateProfile request = new MemberReqDTO.UpdateProfile(
                null,
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                10L
        );

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("미래 생년월일이면 개인정보 수정에 실패한다")
    void updateProfileFailsWhenBirthDateIsFuture() throws Exception {
        MemberReqDTO.UpdateProfile request = new MemberReqDTO.UpdateProfile(
                "김철수",
                LocalDate.now().plusDays(1),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                10L
        );

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("범위를 벗어난 위도이면 개인정보 수정에 실패한다")
    void updateProfileFailsWhenLatitudeIsOutOfRange() throws Exception {
        MemberReqDTO.UpdateProfile request = new MemberReqDTO.UpdateProfile(
                "김철수",
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("91.0000000"),
                new BigDecimal("126.6510000"),
                10L
        );

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    private MemberReqDTO.UpdateProfile createUpdateRequest() {
        return new MemberReqDTO.UpdateProfile(
                "김철수",
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                10L
        );
    }

    private Filter authenticatedMemberFilter() {
        return (request, response, filterChain) -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(MEMBER_ID, null, List.of()));
            SecurityContextHolder.setContext(context);

            try {
                filterChain.doFilter(request, response);
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

}
