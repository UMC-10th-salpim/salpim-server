package salpim.umc10thsalpim.domain.map.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.map.dto.MapRequestDto;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;
import salpim.umc10thsalpim.domain.map.exception.code.MapSuccessCode;
import salpim.umc10thsalpim.domain.map.service.FacilityService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/details")
    @Operation(summary = "시설 상세 정보 조회", description = "시설 이름과 유저의 관할동을 비교하여 정보를 반환합니다.")
    public ApiResponse<MapResponseDto.FacilityInfoResponseDto> getFacilityDetails(
        @RequestParam("memberId") Long memberId,
        @ModelAttribute MapRequestDto.FacilityInfoRequest request
    ){
        MapResponseDto.FacilityInfoResponseDto result = facilityService.getFacilityInfo(memberId, request);
        BaseSuccessCode code = MapSuccessCode.OK;
        return ApiResponse.onSuccess(code,result);
    }

}
