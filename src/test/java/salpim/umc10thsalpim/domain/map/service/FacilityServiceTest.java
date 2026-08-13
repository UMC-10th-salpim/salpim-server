package salpim.umc10thsalpim.domain.map.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.map.converter.WelfareConverter;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.domain.map.exception.MapException;
import salpim.umc10thsalpim.domain.map.exception.code.MapErrorCode;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionQueryService regionQueryService;

    @Mock
    private WelfareBenefitRepository welfareBenefitRepository;

    @Mock
    private WelfareConverter welfareConverter;

    @InjectMocks
    private FacilityService facilityService;

    private Member createMockMember(Long id, String welfareCenter) {
        return Member.builder()
                .id(id)
                .regionId(4L)
                .welfareCenter(welfareCenter)
                .latitude(new BigDecimal("37.4510000"))
                .longitude(new BigDecimal("126.6500000"))
                .build();
    }

    private MapReqDTO.FacilityInfoRequest createMockRequest(String facilityName) {
        return new MapReqDTO.FacilityInfoRequest(
                facilityName,
                "인천 미추홀구 매소홀로",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                null,
                10
        );
    }

    @Nested
    @DisplayName("시설 정보 조회 (getFacilityInfo) - 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("시설 조회 성공 - 다음 페이지 없음 (hasNext = false)")
        void getFacilityInfo_Success_NoNextPage() {
            // given
            Member member = createMockMember(1L, "용현동");
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 행정복지센터");
            Map<Long, String> regionNameMap = Map.of(4L, "미추홀구");

            WelfareBenefit benefit1 = WelfareBenefit.builder().id(10L).title("혜택1").build();
            List<WelfareBenefit> benefitsFromDb = new ArrayList<>(List.of(benefit1));

            MapResDTO.BenefitDTO benefitDTO1 = MapResDTO.BenefitDTO.builder()
                    .benefitId(10L)
                    .servId("WLF00000001")
                    .region("전국")
                    .serviceName("혜택1")
                    .build();

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(regionQueryService.getAncestorRegionNameMap(4L)).thenReturn(regionNameMap);
            when(welfareBenefitRepository.findWelfareBenefitsByRegionAndCursorAndAppType(
                    eq(List.of(4L)),
                    eq(ApplicationType.VISIT),
                    eq(0L),
                    any()
            )).thenReturn(benefitsFromDb);
            when(welfareConverter.toBenefitDTOList(eq(benefitsFromDb), eq(regionNameMap)))
                    .thenReturn(List.of(benefitDTO1));
            when(welfareConverter.toFacilityInfoResDTO(eq(request), anyString(), eq(true), any()))
                    .thenReturn(MapResDTO.FacilityInfoResDTO.builder()
                            .name("용현동 행정복지센터")
                            .isMyCenter(true)
                            .build());

            // when
            MapResDTO.FacilityInfoResDTO result = facilityService.getFacilityInfo(1L, request, null, 10);

            // then
            assertNotNull(result);
            assertEquals("용현동 행정복지센터", result.name());
            assertTrue(result.isMyCenter());

            verify(memberRepository).findById(1L);
            verify(regionQueryService).getAncestorRegionNameMap(4L);
            verify(welfareBenefitRepository).findWelfareBenefitsByRegionAndCursorAndAppType(
                    eq(List.of(4L)), eq(ApplicationType.VISIT), eq(0L), any()
            );
        }

        @Test
        @DisplayName("시설 조회 성공 - 다음 페이지 있음 (hasNext = true)")
        void getFacilityInfo_Success_WithNextPage() {
            // given
            Member member = createMockMember(1L, "용현동");
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 주민센터");
            Map<Long, String> regionNameMap = Map.of(4L, "미추홀구");

            int requestedSize = 2;
            WelfareBenefit benefit1 = WelfareBenefit.builder().id(10L).title("혜택1").build();
            WelfareBenefit benefit2 = WelfareBenefit.builder().id(20L).title("혜택2").build();
            WelfareBenefit benefit3 = WelfareBenefit.builder().id(30L).title("혜택3").build();
            // DB에서 size + 1 (3개) 반환 -> hasNext = true
            List<WelfareBenefit> benefitsFromDb = new ArrayList<>(List.of(benefit1, benefit2, benefit3));

            MapResDTO.BenefitDTO benefitDTO1 = MapResDTO.BenefitDTO.builder().benefitId(10L).servId("WLF1").build();
            MapResDTO.BenefitDTO benefitDTO2 = MapResDTO.BenefitDTO.builder().benefitId(20L).servId("WLF2").build();

            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(regionQueryService.getAncestorRegionNameMap(4L)).thenReturn(regionNameMap);
            when(welfareBenefitRepository.findWelfareBenefitsByRegionAndCursorAndAppType(
                    eq(List.of(4L)), eq(ApplicationType.VISIT), eq(100L), any()
            )).thenReturn(benefitsFromDb);

            when(welfareConverter.toBenefitDTOList(anyList(), eq(regionNameMap)))
                    .thenReturn(List.of(benefitDTO1, benefitDTO2));
            when(welfareConverter.toFacilityInfoResDTO(eq(request), anyString(), eq(true), any()))
                    .thenAnswer(invocation -> {
                        MapResDTO.BenefitPageDTO pageDTO = invocation.getArgument(3);
                        assertTrue(pageDTO.hasNext());
                        assertEquals("20", pageDTO.nextCursor());
                        assertEquals(2, pageDTO.pageSize());
                        return MapResDTO.FacilityInfoResDTO.builder().build();
                    });

            // when
            MapResDTO.FacilityInfoResDTO result = facilityService.getFacilityInfo(1L, request, "100", requestedSize);

            // then
            assertNotNull(result);
            verify(welfareBenefitRepository).findWelfareBenefitsByRegionAndCursorAndAppType(
                    eq(List.of(4L)), eq(ApplicationType.VISIT), eq(100L), any()
            );
        }
    }

    @Nested
    @DisplayName("시설 정보 조회 (getFacilityInfo) - 예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("존재하지 않는 회원 ID인 경우 MemberException 예외 발생")
        void getFacilityInfo_MemberNotFound_ThrowsMemberException() {
            // given
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 행정복지센터");
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            MemberException exception = assertThrows(MemberException.class,
                    () -> facilityService.getFacilityInfo(999L, request, null, 10));

            assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("회원의 관할 행정복지센터 정보가 없는 경우 MapException(SERVICE_CENTER_NOT_FOUND) 발생")
        void getFacilityInfo_NoWelfareCenter_ThrowsMapException() {
            // given
            Member member = createMockMember(1L, null); // welfareCenter가 null
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 행정복지센터");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // when & then
            MapException exception = assertThrows(MapException.class,
                    () -> facilityService.getFacilityInfo(1L, request, null, 10));

            assertEquals(MapErrorCode.SERVICE_CENTER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("요청 시설명이 null이거나 빈 값인 경우 MapException(INVALID_FACILITY_REQUEST) 발생")
        void getFacilityInfo_InvalidFacilityRequest_ThrowsMapException() {
            // given
            Member member = createMockMember(1L, "용현동");
            MapReqDTO.FacilityInfoRequest request = createMockRequest(""); // 빈 값
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // when & then
            MapException exception = assertThrows(MapException.class,
                    () -> facilityService.getFacilityInfo(1L, request, null, 10));

            assertEquals(MapErrorCode.INVALID_FACILITY_REQUEST, exception.getErrorCode());
        }

        @Test
        @DisplayName("요청 시설이 행정복지센터나 주민센터가 아닌 경우 MapException(NOT_WELFARE_CENTER) 발생")
        void getFacilityInfo_NotWelfareCenter_ThrowsMapException() {
            // given
            Member member = createMockMember(1L, "용현동");
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 우체국");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // when & then
            MapException exception = assertThrows(MapException.class,
                    () -> facilityService.getFacilityInfo(1L, request, null, 10));

            assertEquals(MapErrorCode.NOT_WELFARE_CENTER, exception.getErrorCode());
        }

        @Test
        @DisplayName("요청 시설이 회원의 관할 동과 일치하지 않는 경우 MapException(NOT_MY_SERVICE_CENTER) 발생")
        void getFacilityInfo_NotMyServiceCenter_ThrowsMapException() {
            // given
            Member member = createMockMember(1L, "용현동");
            MapReqDTO.FacilityInfoRequest request = createMockRequest("주안1동 행정복지센터");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // when & then
            MapException exception = assertThrows(MapException.class,
                    () -> facilityService.getFacilityInfo(1L, request, null, 10));

            assertEquals(MapErrorCode.NOT_MY_SERVICE_CENTER, exception.getErrorCode());
        }

        @Test
        @DisplayName("유효하지 않은 커서 형식인 경우 MapException(INVALID_CURSOR) 발생")
        void getFacilityInfo_InvalidCursor_ThrowsMapException() {
            // given
            MapReqDTO.FacilityInfoRequest request = createMockRequest("용현동 행정복지센터");

            // when & then
            MapException exception = assertThrows(MapException.class,
                    () -> facilityService.getFacilityInfo(1L, request, "invalid_cursor", 10));

            assertEquals(MapErrorCode.INVALID_CURSOR, exception.getErrorCode());
        }
    }
}
