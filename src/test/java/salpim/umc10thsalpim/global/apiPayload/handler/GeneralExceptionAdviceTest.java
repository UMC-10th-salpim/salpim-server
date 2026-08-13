package salpim.umc10thsalpim.global.apiPayload.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeneralExceptionAdviceTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/path/{id}")
        public String getPath(@PathVariable Long id) {
            return "ok";
        }

        @GetMapping("/test/param")
        public String getParam(@RequestParam String name) {
            return "ok";
        }

        @GetMapping("/test/supported-get")
        public String getOnly() {
            return "ok";
        }

        @GetMapping("/test/no-resource")
        public String noResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "/test/no-resource", "Resource not found");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GeneralExceptionAdvice())
                .build();
    }

    @Test
    @DisplayName("잘못된 숫자/Path variable 요청 시 HTTP 400 및 COMMON400 반환")
    void typeMismatch_returnsCommon400() throws Exception {
        mockMvc.perform(get("/test/path/invalid_id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("COMMON400")));
    }

    @Test
    @DisplayName("필수 query parameter 누락 시 HTTP 400 및 COMMON400 반환")
    void missingParameter_returnsCommon400() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("COMMON400")));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드 요청 시 HTTP 405 및 COMMON405 반환")
    void methodNotSupported_returnsCommon405() throws Exception {
        mockMvc.perform(post("/test/supported-get"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("COMMON405")));
    }

    @Test
    @DisplayName("존재하지 않는 정적 리소스 경로 요청 시 HTTP 404 및 COMMON404 반환")
    void noResourceFound_returnsCommon404() throws Exception {
        mockMvc.perform(get("/test/no-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("COMMON404")));
    }
}
