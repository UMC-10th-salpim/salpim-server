package salpim.umc10thsalpim.domain.region.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.exception.code.RegionSuccessCode;
import salpim.umc10thsalpim.domain.region.service.RegionService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "Region", description = "Region API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionService regionService;

    @Operation(
            summary = "행정구역 계층 저장 및 조회",
            description = "시/도, 시/군/구, 일반구, 행정구역 정보를 계층형 Region으로 저장하거나 기존 Region을 조회한 후 가장 하위 지역 ID를 반환합니다."
    )
    @PostMapping("/resolve")
    public ResponseEntity<ApiResponse<RegionResDTO.ResolveResult>> resolve(
            @Valid @RequestBody RegionReqDTO.Resolve request
    ) {
        RegionResDTO.ResolveResult response = regionService.resolve(request);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @GetMapping
    @Operation(
            summary = "상위 지역 list 반환",
            description = """
                지역 선택에서 시/도 부분의 list를 반화합니다
                """
    )
    public ApiResponse<RegionResDTO.RegionListDTO> getAncestorRegionList() {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_LIST_GET_SUCCESS, regionService.getAncestorRegionList());
    }

    @GetMapping("/{ancestorRegionId}/children")
    @Operation(
            summary = "하위 지역 list 반환",
            description = """
                상위 지역 선택 후 시/군/구 부분의 list를 반화합니다
                """
    )
    public ApiResponse<RegionResDTO.RegionListDTO> getDescendantRegionList(
            @PathVariable Long ancestorRegionId
    ) {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_LIST_GET_SUCCESS, regionService.getDescendantRegionList(ancestorRegionId));
    }
}
