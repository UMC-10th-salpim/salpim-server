package salpim.umc10thsalpim.domain.region.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.service.RegionService;
import salpim.umc10thsalpim.global.apiPayload.handler.GeneralExceptionAdvice;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegionControllerTest {

    @Mock
    private RegionService regionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RegionController(regionService))
                .setControllerAdvice(new GeneralExceptionAdvice())
                .setValidator(validator())
                .build();
    }

    @Test
    void resolveReturnsRegionId() throws Exception {
        when(regionService.resolve(any(RegionReqDTO.Resolve.class)))
                .thenReturn(new RegionResDTO.ResolveResult(13L, "Hwajeon", "Goyang Deogyang Hwajeon"));

        mockMvc.perform(post("/api/regions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "Goyang",
                                  "district": "Deogyang",
                                  "eupMyeonDong": "Hwajeon"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.code", is("COMMON200")))
                .andExpect(jsonPath("$.result.regionId", is(13)))
                .andExpect(jsonPath("$.result.regionName", is("Hwajeon")))
                .andExpect(jsonPath("$.result.fullRegionName", is("Goyang Deogyang Hwajeon")));
    }

    @Test
    void resolveFailsWhenEupMyeonDongIsBlank() throws Exception {
        mockMvc.perform(post("/api/regions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "city": "Goyang",
                                  "district": "Deogyang",
                                  "eupMyeonDong": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("COMMON400")));
    }

    private Validator validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        return validatorFactoryBean;
    }
}
