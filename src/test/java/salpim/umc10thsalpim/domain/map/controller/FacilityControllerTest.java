package salpim.umc10thsalpim.domain.map.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import salpim.umc10thsalpim.domain.map.exception.MapException;
import salpim.umc10thsalpim.domain.map.exception.code.MapErrorCode;
import salpim.umc10thsalpim.domain.map.service.FacilityService;
import salpim.umc10thsalpim.global.apiPayload.handler.GeneralExceptionAdvice;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FacilityControllerTest {

    @Mock
    private FacilityService facilityService;

    @InjectMocks
    private FacilityController facilityController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(facilityController)
                .setControllerAdvice(new GeneralExceptionAdvice())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("지도 API에 문자열 cursor 전달 시 MAP400_5 반환")
    void getFacilityDetails_invalidCursor_returnsMap400_5() throws Exception {
        when(facilityService.getFacilityInfo(any(), any(), eq("invalid_cursor"), anyInt()))
                .thenThrow(new MapException(MapErrorCode.INVALID_CURSOR));

        mockMvc.perform(get("/api/map/details")
                        .param("facilityName", "용현동 행정복지센터")
                        .param("address", "인천 미추홀구")
                        .param("latitude", "37.4520000")
                        .param("longitude", "126.6510000")
                        .param("cursor", "invalid_cursor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess", is(false)))
                .andExpect(jsonPath("$.code", is("MAP400_5")))
                .andExpect(jsonPath("$.message", is("유효하지 않은 커서 값입니다.")));
    }
}
