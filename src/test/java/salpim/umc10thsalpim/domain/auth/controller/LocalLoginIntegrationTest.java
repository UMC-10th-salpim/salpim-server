package salpim.umc10thsalpim.domain.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
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

        mockMvc.perform(post("/api/login/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "01000000000",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_LOGIN_CREDENTIALS"));

        assertThat(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                UNREGISTERED_PHONE_NUMBER
        )).isPresent();
        assertThat(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                "127.0.0.1"
        )).isPresent();
    }
}
