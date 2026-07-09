package salpim.umc10thsalpim.domain.map.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ApiResponse<MapResponseDto.FacilityInfoResponse> getFacilityDetails(
        @RequestParam("memberId") Long memberId,
        @RequestParam String facilityName,
        @RequestParam String address,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String distance
    ){
        MapResponseDto.FacilityInfoResponse result = facilityService.getFacilityInfo(
                memberId, facilityName, address, phone, distance
        );
        BaseSuccessCode code = MapSuccessCode.OK;
        return ApiResponse.onSuccess(code,result);
    }

}
