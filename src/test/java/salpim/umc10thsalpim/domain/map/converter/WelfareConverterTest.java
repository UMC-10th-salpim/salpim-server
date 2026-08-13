package salpim.umc10thsalpim.domain.map.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WelfareConverterTest {

    private WelfareConverter welfareConverter;

    @BeforeEach
    void setUp() {
        welfareConverter = new WelfareConverter();
    }

    @Test
    @DisplayName("FacilityInfoResDTO 변환 테스트")
    void toFacilityInfoResDTO_Success() {
        // given
        MapReqDTO.FacilityInfoRequest request = new MapReqDTO.FacilityInfoRequest(
                "용현동 행정복지센터",
                "인천 미추홀구 매소홀로",
                new BigDecimal("37.452"),
                new BigDecimal("126.651"),
                null,
                10
        );
        MapResDTO.BenefitPageDTO pageDTO = MapResDTO.BenefitPageDTO.builder().data(Collections.emptyList()).build();

        // when
        MapResDTO.FacilityInfoResDTO resDTO = welfareConverter.toFacilityInfoResDTO(request, "1.2km", true, pageDTO);

        // then
        assertNotNull(resDTO);
        assertEquals("용현동 행정복지센터", resDTO.name());
        assertEquals("인천 미추홀구 매소홀로", resDTO.address());
        assertEquals("09:00 - 18:00", resDTO.hour());
        assertEquals("1.2km", resDTO.distanceText());
        assertTrue(resDTO.isMyCenter());
        assertEquals(pageDTO, resDTO.benefits());
    }

    @Test
    @DisplayName("BenefitDTO 리스트 변환 - 빈 리스트나 null 처리")
    void toBenefitDTOList_NullOrEmpty() {
        assertTrue(welfareConverter.toBenefitDTOList(null, Map.of()).isEmpty());
        assertTrue(welfareConverter.toBenefitDTOList(Collections.emptyList(), Map.of()).isEmpty());
    }

    @Test
    @DisplayName("BenefitDTO 리스트 변환 - 중앙 혜택 및 지자체 혜택 구분 처리")
    void toBenefitDTOList_Success() {
        // given
        WelfareBenefit centralBenefit = WelfareBenefit.builder()
                .id(1L)
                .externalId("WLF0001")
                .source("CENTRAL")
                .title("중앙 혜택")
                .build();

        WelfareBenefit localBenefit = WelfareBenefit.builder()
                .id(2L)
                .externalId("WLF0002")
                .source("LOCAL")
                .regionId(4L)
                .title("지자체 혜택")
                .build();

        Map<Long, String> regionNameMap = Map.of(4L, "인천 미추홀구");

        // when
        List<MapResDTO.BenefitDTO> result = welfareConverter.toBenefitDTOList(List.of(centralBenefit, localBenefit), regionNameMap);

        // then
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).benefitId());
        assertEquals("WLF0001", result.get(0).servId());
        assertEquals("전국", result.get(0).region());
        assertEquals("중앙 혜택", result.get(0).serviceName());

        assertEquals(2L, result.get(1).benefitId());
        assertEquals("WLF0002", result.get(1).servId());
        assertEquals("인천 미추홀구", result.get(1).region());
        assertEquals("지자체 혜택", result.get(1).serviceName());
    }
}
