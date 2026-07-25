package salpim.umc10thsalpim.domain.map.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FacilityServiceTest {

    private final FacilityService facilityService = new FacilityService(null, null, null, null);

    private List<MapResponseDTO.BenefitDto> createMockBenefits(int count) {
        List<MapResponseDTO.BenefitDto> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(MapResponseDTO.BenefitDto.builder()
                    .servId("WLF" + String.format("%07d", i))
                    .region("전국")
                    .serviceName("혜택 " + i)
                    .build());
        }
        return list;
    }

    @Test
    @DisplayName("커서가 null일 때 첫 번째 페이지 조회 (size=10, 총 25개)")
    void firstPageTest() {
        List<MapResponseDTO.BenefitDto> mockData = createMockBenefits(25);

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, null, 10);

        assertEquals(10, result.pageSize());
        assertTrue(result.hasNext());
        assertEquals("WLF0000010", result.nextCursor());
        assertEquals(25, result.totalCount());
        assertEquals(10, result.data().size());
        assertEquals("WLF0000001", result.data().get(0).servId());
        assertEquals("WLF0000010", result.data().get(9).servId());
    }

    @Test
    @DisplayName("nextCursor로 다음 페이지 조회 (size=10, 총 25개)")
    void secondPageTest() {
        List<MapResponseDTO.BenefitDto> mockData = createMockBenefits(25);

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, "WLF0000010", 10);

        assertEquals(10, result.pageSize());
        assertTrue(result.hasNext());
        assertEquals("WLF0000020", result.nextCursor());
        assertEquals(25, result.totalCount());
        assertEquals(10, result.data().size());
        assertEquals("WLF0000011", result.data().get(0).servId());
        assertEquals("WLF0000020", result.data().get(9).servId());
    }

    @Test
    @DisplayName("마지막 페이지 조회 (size=10, 총 25개중 20번째 커서 이후)")
    void lastPageTest() {
        List<MapResponseDTO.BenefitDto> mockData = createMockBenefits(25);

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, "WLF0000020", 10);

        assertEquals(5, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(25, result.totalCount());
        assertEquals(5, result.data().size());
        assertEquals("WLF0000021", result.data().get(0).servId());
        assertEquals("WLF0000025", result.data().get(4).servId());
    }

    @Test
    @DisplayName("마지막 아이템이 커서인 경우 빈 페이지 반환")
    void cursorAtEndTest() {
        List<MapResponseDTO.BenefitDto> mockData = createMockBenefits(25);

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, "WLF0000025", 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(25, result.totalCount());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 커서 입력 시 방어 로직으로 빈 페이지 반환")
    void invalidCursorTest() {
        List<MapResponseDTO.BenefitDto> mockData = createMockBenefits(25);

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, "INVALID_CURSOR", 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(25, result.totalCount());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("데이터가 비어있는 경우")
    void emptyDataTest() {
        List<MapResponseDTO.BenefitDto> mockData = new ArrayList<>();

        MapResponseDTO.BenefitPageDto result = facilityService.paginateBenefits(mockData, null, 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(0, result.totalCount());
        assertTrue(result.data().isEmpty());
    }
}
