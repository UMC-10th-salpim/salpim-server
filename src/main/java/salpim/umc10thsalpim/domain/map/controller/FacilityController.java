package salpim.umc10thsalpim.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/details")
    @SecurityRequirement(name = "JWT TOKEN")
    @Operation(summary = "시설 상세 정보 조회", description = "시설 이름과 유저의 관할동을 비교하여 정보를 반환하며, 혜택 리스트는 커서 기반 페이징으로 응답합니다.")
    public ApiResponse<MapResDTO.FacilityInfoResDTO> getFacilityDetails(
        @AuthenticationPrincipal Long memberId,

        @Valid @ParameterObject @ModelAttribute MapReqDTO.FacilityInfoRequest request,

        @Parameter(description = "다음 페이지 커서 (첫 요청 시 비워둠, 다음 요청 시 이전 응답의 nextCursor 입력)", example = "WLF00001234")
        @RequestParam(value = "cursor", required = false) String cursor,

        @Parameter(description = "페이지 크기 (기본값 10)", example = "10")
        @RequestParam(value = "size", defaultValue = "10") int size
    ){
        String effectiveCursor = (cursor != null && !cursor.isBlank()) ? cursor : request.cursor();
        int effectiveSize = (size > 0) ? size : (request.size() != null && request.size() > 0 ? request.size() : 10);

        MapResDTO.FacilityInfoResDTO result = facilityService.getFacilityInfo(memberId, request, effectiveCursor, effectiveSize);
        BaseSuccessCode code = MapSuccessCode.FACILITY_INFO_SUCCESS;
        return ApiResponse.onSuccess(code, result);
    }

}
