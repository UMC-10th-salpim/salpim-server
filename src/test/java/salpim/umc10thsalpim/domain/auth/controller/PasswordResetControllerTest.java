package salpim.umc10thsalpim.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.service.PasswordResetService;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.global.apiPayload.handler.GeneralExceptionAdvice;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordResetController.class)
class PasswordResetControllerTest {

    private static final String PASSWORD_RESET_TOKEN = "password-reset-token";

    @Autowired
    private PasswordResetController passwordResetController;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController)
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    @Test
    void verifyRecoveryAnswerReturnsPasswordResetToken() throws Exception {
        AuthReqDTO.PasswordResetVerify request = new AuthReqDTO.PasswordResetVerify(
                "01012345678",
                "spring"
        );
        AuthResDTO.PasswordResetVerifyResult response =
                new AuthResDTO.PasswordResetVerifyResult(PASSWORD_RESET_TOKEN);
        given(passwordResetService.verifyRecoveryAnswer(request)).willReturn(response);

        mockMvc.perform(post("/api/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET200_1"))
                .andExpect(jsonPath("$.result.passwordResetToken").value(PASSWORD_RESET_TOKEN));

        verify(passwordResetService).verifyRecoveryAnswer(request);
    }

    @Test
    void resetPasswordReturnsSuccess() throws Exception {
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset(
                PASSWORD_RESET_TOKEN,
                "123456"
        );

        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET200_2"))
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(passwordResetService).resetPassword(request);
    }

    @Test
    void verifyRecoveryAnswerFailsWhenPhoneNumberIsMissing() throws Exception {
        String request = """
                {
                  "recoveryAnswer": "spring"
                }
                """;

        mockMvc.perform(post("/api/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void resetPasswordFailsWhenNewPasswordIsNotSixDigits() throws Exception {
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset(
                PASSWORD_RESET_TOKEN,
                "12345"
        );

        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"));

        verifyNoInteractions(passwordResetService);
    }
}
