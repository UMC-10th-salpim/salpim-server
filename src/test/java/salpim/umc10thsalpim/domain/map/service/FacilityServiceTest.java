package salpim.umc10thsalpim.domain.map.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.map.converter.WelfareConverter;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionQueryService regionQueryService;

    @Mock
    private BokjiroApiClient bokjiroApiClient;

    @Mock
    private WelfareConverter welfareConverter;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void requestsLocalBenefitsWithUpperRegionsForMemberUnderGeneralGu() {
        Member member = Member.builder()
                .id(1L)
                .regionId(4L)
                .welfareCenter("용현동")
                .latitude(new BigDecimal("37.451"))
                .longitude(new BigDecimal("126.650"))
                .build();
        MapReqDTO.FacilityInfoRequest request = new MapReqDTO.FacilityInfoRequest(
                "용현동 행정복지센터",
                "Incheon",
                new BigDecimal("37.452"),
                new BigDecimal("126.651"),
                null,
                10
        );
        BokjiroApiDTO.BenefitListRes response = new BokjiroApiDTO.BenefitListRes();

        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(regionQueryService.getSidoAndSigungu(4L))
                .thenReturn(new String[]{"Incheon", "Michuhol-gu"});
        when(bokjiroApiClient.searchNationalBenefits(1, 100, null, null)).thenReturn(response);
        when(bokjiroApiClient.searchLocalBenefits(1, 100, null, null, "Incheon", "Michuhol-gu"))
                .thenReturn(response);
        when(welfareConverter.toCentralBenefitDTO(response)).thenReturn(List.of());
        when(welfareConverter.toLocalBenefitDTO(response, "Incheon", "Michuhol-gu")).thenReturn(List.of());

        facilityService.getFacilityInfo(1L, request);

        verify(bokjiroApiClient).searchLocalBenefits(
                1,
                100,
                null,
                null,
                "Incheon",
                "Michuhol-gu"
        );
    }

    private List<MapResDTO.BenefitDTO> createMockBenefits(int count) {
        List<MapResDTO.BenefitDTO> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(MapResDTO.BenefitDTO.builder()
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
        List<MapResDTO.BenefitDTO> mockData = createMockBenefits(25);

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, null, 10);

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
        List<MapResDTO.BenefitDTO> mockData = createMockBenefits(25);

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, "WLF0000010", 10);

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
        List<MapResDTO.BenefitDTO> mockData = createMockBenefits(25);

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, "WLF0000020", 10);

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
        List<MapResDTO.BenefitDTO> mockData = createMockBenefits(25);

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, "WLF0000025", 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(25, result.totalCount());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 커서 입력 시 방어 로직으로 빈 페이지 반환")
    void invalidCursorTest() {
        List<MapResDTO.BenefitDTO> mockData = createMockBenefits(25);

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, "INVALID_CURSOR", 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(25, result.totalCount());
        assertTrue(result.data().isEmpty());
    }

    @Test
    @DisplayName("데이터가 비어있는 경우")
    void emptyDataTest() {
        List<MapResDTO.BenefitDTO> mockData = new ArrayList<>();

        MapResDTO.BenefitPageDTO result = facilityService.paginateBenefits(mockData, null, 10);

        assertEquals(0, result.pageSize());
        assertFalse(result.hasNext());
        assertNull(result.nextCursor());
        assertEquals(0, result.totalCount());
        assertTrue(result.data().isEmpty());
    }
}
