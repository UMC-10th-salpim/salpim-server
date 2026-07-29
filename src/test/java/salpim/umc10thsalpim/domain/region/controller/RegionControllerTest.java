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
    void resolveReturnsAdministrativeAreaForThreeLevelRequest() throws Exception {
        when(regionService.resolve(any(RegionReqDTO.Resolve.class)))
                .thenReturn(new RegionResDTO.ResolveResult(
                        13L,
                        "Yonghyeon-dong",
                        "Incheon Michuhol-gu Yonghyeon-dong"
                ));

        mockMvc.perform(post("/api/regions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sido": "Incheon",
                                  "sigungu": "Michuhol-gu",
                                  "administrativeArea": "Yonghyeon-dong"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess", is(true)))
                .andExpect(jsonPath("$.result.regionId", is(13)))
                .andExpect(jsonPath("$.result.regionName", is("Yonghyeon-dong")))
                .andExpect(jsonPath("$.result.fullRegionName", is("Incheon Michuhol-gu Yonghyeon-dong")));
    }

    @Test
    void resolveReturnsAdministrativeAreaForFourLevelRequest() throws Exception {
        when(regionService.resolve(any(RegionReqDTO.Resolve.class)))
                .thenReturn(new RegionResDTO.ResolveResult(
                        14L,
                        "Hwajeong-dong",
                        "Gyeonggi-do Goyang-si Deogyang-gu Hwajeong-dong"
                ));

        mockMvc.perform(post("/api/regions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sido": "Gyeonggi-do",
                                  "sigungu": "Goyang-si",
                                  "generalGu": "Deogyang-gu",
                                  "administrativeArea": "Hwajeong-dong"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.regionId", is(14)));
    }

    @Test
    void resolveFailsWhenSidoIsMissing() throws Exception {
        assertInvalidRequest("""
                {
                  "sigungu": "Michuhol-gu",
                  "administrativeArea": "Yonghyeon-dong"
                }
                """);
    }

    @Test
    void resolveFailsWhenSigunguIsMissing() throws Exception {
        assertInvalidRequest("""
                {
                  "sido": "Incheon",
                  "administrativeArea": "Yonghyeon-dong"
                }
                """);
    }

    @Test
    void resolveFailsWhenAdministrativeAreaIsBlank() throws Exception {
        assertInvalidRequest("""
                {
                  "sido": "Incheon",
                  "sigungu": "Michuhol-gu",
                  "administrativeArea": ""
                }
                """);
    }

    private void assertInvalidRequest(String requestBody) throws Exception {
        mockMvc.perform(post("/api/regions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
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
