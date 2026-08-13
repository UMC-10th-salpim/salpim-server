package salpim.umc10thsalpim.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.map.dto.MapReqDTO;
import salpim.umc10thsalpim.domain.map.dto.MapResDTO;
import salpim.umc10thsalpim.domain.map.exception.code.MapSuccessCode;
import salpim.umc10thsalpim.domain.map.service.FacilityService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@Tag(name = "지도", description = "지도 내 시설(행정복지센터 등) 관련 정보 및 혜택 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/details")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(summary = "시설 상세 정보 조회",
            description = """
                지도에서 선택한 시설의 상세 정보와, 해당 지역에서 방문 신청할 수 있는 복지 혜택 목록을 함께 조회합니다.
                카카오맵에서 받은 시설 이름을 회원의 관할 행정동과 비교해, 본인 관할 센터인 경우에만 정보를 반환합니다.
                혜택 리스트는 커서 기반 페이징으로 응답합니다.
                - name / address: 요청으로 보낸 값을 그대로 돌려줍니다.
                - distanceText: 회원 좌표와 시설 좌표 사이의 직선거리이며, 1km 미만은 m 단위(예: 800m), 1km 이상은 km 단위(예: 1.2km)입니다.
                - benefits.data: 회원 지역과 그 상위 지역의 혜택 중 방문(VISIT) 신청이 가능한 혜택만 담깁니다.
                - benefits.data[].benefitId: 다음 페이지 조회에 사용할 커서 값입니다.
                - benefits.data[].servId: 혜택 상세 조회 등에 사용하는 복지로 서비스 ID입니다.
                - benefits.data[].region: 중앙부처 혜택은 "전국", 지자체 혜택은 해당 지역명입니다.
                """
    )
    public ApiResponse<MapResDTO.FacilityInfoResDTO> getFacilityDetails(
        @AuthenticationPrincipal Long memberId,

        @Valid @ParameterObject @ModelAttribute MapReqDTO.FacilityInfoRequest request,

        @Parameter(description = "다음 페이지 커서 (첫 요청 시 비워둠, 다음 요청 시 이전 응답의 nextCursor 입력)")
        @RequestParam(value = "cursor", required = false) String cursor,

        @Parameter(description = "한 페이지에 반환할 개수", example = "10")
        @RequestParam(value = "size", defaultValue = "10") int size
    ){
        String effectiveCursor = (cursor != null && !cursor.isBlank()) ? cursor : request.cursor();
        int effectiveSize = (size > 0) ? size : (request.size() != null && request.size() > 0 ? request.size() : 10);

        MapResDTO.FacilityInfoResDTO result = facilityService.getFacilityInfo(memberId, request, effectiveCursor, effectiveSize);
        BaseSuccessCode code = MapSuccessCode.FACILITY_INFO_SUCCESS;
        return ApiResponse.onSuccess(code, result);
    }

}
