package salpim.umc10thsalpim.domain.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void localLoginOpenApiContainsOnlyJsonRequestBody() throws Exception {
        String operationPath = "$['paths']['/api/login/local']['post']";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        operationPath + "['requestBody']['content']['application/json']['schema']['$ref']",
                        endsWith("/LocalLogin")
                ))
                .andExpect(jsonPath(
                        "$['components']['schemas']['LocalLogin']['properties']['password']['example']"
                ).value("123456"))
                .andExpect(jsonPath(operationPath + "['parameters']").doesNotExist());
    }
}
