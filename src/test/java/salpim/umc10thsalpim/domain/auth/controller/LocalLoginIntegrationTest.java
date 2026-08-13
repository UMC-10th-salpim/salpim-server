package salpim.umc10thsalpim.domain.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LocalLoginIntegrationTest {

    private static final String UNREGISTERED_PHONE_NUMBER = "01000000000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordVerificationAttemptRepository attemptRepository;

    @Test
    void unregisteredPhoneReturnsUnauthorizedAndRecordsFailures() throws Exception {
        assertThat(memberRepository.findByPhoneNumber(UNREGISTERED_PHONE_NUMBER)).isEmpty();
        clearLoginAttempt(PasswordVerificationTargetType.PHONE_NUMBER, UNREGISTERED_PHONE_NUMBER);
        clearLoginAttempt(PasswordVerificationTargetType.IP_ADDRESS, "127.0.0.1");

        mockMvc.perform(post("/api/login/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "01000000000",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(
                        AuthErrorCode.INVALID_LOGIN_CREDENTIALS.getCode()
                ));

        assertThat(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                UNREGISTERED_PHONE_NUMBER
        )).get().extracting("failureCount").isEqualTo(1);
        assertThat(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                "127.0.0.1"
        )).get().extracting("failureCount").isEqualTo(1);
    }

    private void clearLoginAttempt(
            PasswordVerificationTargetType targetType,
            String targetValue
    ) {
        attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.LOGIN,
                targetType,
                targetValue
        ).ifPresent(attemptRepository::delete);
        attemptRepository.flush();
    }
}
