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
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.auth.service.AuthSecretHasher;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.member.enums.PasswordVerificationMethod;
import salpim.umc10thsalpim.domain.member.enums.WordSize;
import salpim.umc10thsalpim.domain.member.service.MemberService;
import salpim.umc10thsalpim.domain.member.service.MemberWithdrawalService;
import salpim.umc10thsalpim.global.apiPayload.handler.GeneralExceptionAdvice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private PhoneVerificationService phoneVerificationService;

    @MockitoBean
    private MemberWithdrawalService memberWithdrawalService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AuthSecretHasher authSecretHasher;

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
                new MemberResDTO.MyPageInfo(
                        "홍길동",
                        LocalDate.of(1950, 1, 1),
                        Gender.MALE,
                        "01012345678",
                        "인천광역시 미추홀구",
                        "101호",
                        new BigDecimal("37.4510000"),
                        new BigDecimal("126.6500000"),
                        3L,
                        "인천광역시",
                        "미추홀구",
                        "덕양구",
                        "용현동"
                );

        given(memberService.getMyPage(MEMBER_ID)).willReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_1"))
                .andExpect(jsonPath("$.result.name").value("홍길동"))
                .andExpect(jsonPath("$.result.birthDate").value("1950-01-01"))
                .andExpect(jsonPath("$.result.gender").value("MALE"))
                .andExpect(jsonPath("$.result.phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.result.roadAddress").value("인천광역시 미추홀구"))
                .andExpect(jsonPath("$.result.detailAddress").value("101호"))
                .andExpect(jsonPath("$.result.latitude").value(37.451))
                .andExpect(jsonPath("$.result.longitude").value(126.65))
                .andExpect(jsonPath("$.result.regionId").value(3))
                .andExpect(jsonPath("$.result.sido").value("인천광역시"))
                .andExpect(jsonPath("$.result.sigungu").value("미추홀구"))
                .andExpect(jsonPath("$.result.generalGu").value("덕양구"))
                .andExpect(jsonPath("$.result.administrativeArea").value("용현동"));

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
    @DisplayName("글자 크기를 정상 수정한다")
    void updateWordSizeSuccess() throws Exception {
        MemberReqDTO.UpdateWordSize request = new MemberReqDTO.UpdateWordSize(WordSize.LARGE);

        mockMvc.perform(put("/api/users/me/word-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_6"));

        verify(memberService).updateWordSize(MEMBER_ID, WordSize.LARGE);
    }

    @Test
    @DisplayName("글자 크기가 없으면 수정에 실패한다")
    void updateWordSizeFailsWhenWordSizeIsMissing() throws Exception {
        mockMvc.perform(put("/api/users/me/word-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("지원하지 않는 글자 크기면 수정에 실패한다")
    void updateWordSizeFailsWhenWordSizeIsInvalid() throws Exception {
        mockMvc.perform(put("/api/users/me/word-size")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wordSize\":\"HUGE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("전화번호 인증 토큰을 포함해 개인정보를 수정한다")
    void updateProfileWithPhoneNumberSuccess() throws Exception {
        MemberReqDTO.UpdateProfile request = createUpdateRequest(
                "010-1234-5678",
                "phone-verification-token"
        );

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_2"));

        verify(memberService).updateProfile(MEMBER_ID, request);
    }

    @Test
    @DisplayName("전화번호 변경 인증번호를 발송한다")
    void sendPhoneChangeVerificationCodeSuccess() throws Exception {
        AuthReqDTO.PhoneSend request = new AuthReqDTO.PhoneSend("010-1234-5678");

        mockMvc.perform(post("/api/users/me/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH200_PHONE_SENT"));

        verify(phoneVerificationService).sendPhoneChangeVerificationCode(
                MEMBER_ID,
                request.phoneNumber()
        );
    }

    @Test
    @DisplayName("전화번호 변경 인증번호를 검증하고 인증 토큰을 발급한다")
    void verifyPhoneChangeCodeSuccess() throws Exception {
        AuthReqDTO.PhoneVerify request = new AuthReqDTO.PhoneVerify("010-1234-5678", "123456");
        AuthResDTO.PhoneChangeVerifyResult response =
                new AuthResDTO.PhoneChangeVerifyResult("phone-verification-token");

        given(phoneVerificationService.verifyPhoneChangeCode(
                MEMBER_ID,
                request.phoneNumber(),
                request.code()
        )).willReturn(response);

        mockMvc.perform(post("/api/users/me/phone-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH200_PHONE_VERIFIED"))
                .andExpect(jsonPath("$.result.phoneVerificationToken")
                        .value("phone-verification-token"));

        verify(phoneVerificationService).verifyPhoneChangeCode(
                MEMBER_ID,
                request.phoneNumber(),
                request.code()
        );
    }

    @Test
    @DisplayName("전화번호가 비어 있으면 인증번호 발송에 실패한다")
    void sendPhoneChangeVerificationCodeFailsWhenPhoneNumberIsBlank() throws Exception {
        AuthReqDTO.PhoneSend request = new AuthReqDTO.PhoneSend("");

        mockMvc.perform(post("/api/users/me/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    @DisplayName("1분 이내 재발송 요청은 제한한다")
    void sendPhoneChangeVerificationCodeFailsWhenRequestedWithinOneMinute() throws Exception {
        AuthReqDTO.PhoneSend request = new AuthReqDTO.PhoneSend("010-1234-5678");
        willThrow(new AuthException(AuthErrorCode.PHONE_VERIFICATION_RESEND_TOO_SOON))
                .given(phoneVerificationService)
                .sendPhoneChangeVerificationCode(MEMBER_ID, request.phoneNumber());

        mockMvc.perform(post("/api/users/me/phone-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH429_2"));
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
                10L,
                null,
                null
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
                10L,
                null,
                null
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
                10L,
                null,
                null
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
    @DisplayName("현재 비밀번호를 정상 확인한다")
    void verifyCurrentPasswordSuccess() throws Exception {
        MemberReqDTO.VerifyCurrentPassword request =
                new MemberReqDTO.VerifyCurrentPassword("123456");

        MemberResDTO.PasswordVerificationResult response =
                new MemberResDTO.PasswordVerificationResult(true);

        given(memberService.verifyCurrentPassword(1L, request))
                .willReturn(response);

        mockMvc.perform(post("/api/users/me/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_3"))
                .andExpect(jsonPath("$.result.isVerified").value(true));

        verify(memberService).verifyCurrentPassword(1L, request);
    }

    @Test
    @DisplayName("비밀번호 복구 답변을 정상 확인한다")
    void verifyRecoveryAnswerSuccess() throws Exception {
        MemberReqDTO.VerifyRecoveryAnswer request =
                new MemberReqDTO.VerifyRecoveryAnswer("봄");

        MemberResDTO.PasswordVerificationResult response =
                new MemberResDTO.PasswordVerificationResult(true);

        given(memberService.verifyRecoveryAnswer(1L, request))
                .willReturn(response);

        mockMvc.perform(post("/api/users/me/password/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_3"))
                .andExpect(jsonPath("$.result.isVerified").value(true));

        verify(memberService).verifyRecoveryAnswer(1L, request);
    }

    @Test
    @DisplayName("현재 비밀번호 검증 방식으로 비밀번호를 정상 변경한다")
    void changePasswordSuccess() throws Exception {
        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.CURRENT_PASSWORD,
                "123456",
                null,
                "654321"
        );

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_4"));

        verify(memberService).changePassword(1L, request);
    }

    @Test
    @DisplayName("현재 비밀번호가 6자리가 아니면 확인에 실패한다")
    void verifyCurrentPasswordFailsWhenPasswordLengthIsInvalid() throws Exception {
        MemberReqDTO.VerifyCurrentPassword request =
                new MemberReqDTO.VerifyCurrentPassword("12345");

        mockMvc.perform(post("/api/users/me/password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    @Test
    @DisplayName("새 비밀번호가 없으면 비밀번호 변경에 실패한다")
    void changePasswordFailsWhenNewPasswordIsMissing() throws Exception {
        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.CURRENT_PASSWORD,
                "123456",
                null,
                null
        );

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(memberService);
    }

    private MemberReqDTO.UpdateProfile createUpdateRequest() {
        return createUpdateRequest(null, null);
    }

    private MemberReqDTO.UpdateProfile createUpdateRequest(
            String phoneNumber,
            String phoneVerificationToken
    ) {
        return new MemberReqDTO.UpdateProfile(
                "김철수",
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                10L,
                phoneNumber,
                phoneVerificationToken
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
