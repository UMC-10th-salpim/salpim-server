package salpim.umc10thsalpim.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("공개 복지 혜택 상세 조회는 인증 없이 접근할 수 있다")
    void publicBenefitDetailIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/benefits/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("비로그인 비밀번호 재설정 검증 요청은 인증 없이 접근할 수 있다")
    void passwordResetVerificationIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/password-reset/verify")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("마이페이지 조회는 인증이 필요하다")
    void myPageRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("찜 혜택 조회는 인증이 필요하다")
    void favoriteBenefitsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/benefits/favorites"))
                .andExpect(status().isUnauthorized());
    }
}
