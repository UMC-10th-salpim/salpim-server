package salpim.umc10thsalpim.domain.region.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.exception.code.RegionSuccessCode;
import salpim.umc10thsalpim.domain.region.service.RegionService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/regions")
@Tag(name = "지역", description = "행정구역 조회 및 저장 API")
public class RegionController {

    private final RegionService regionService;

    @PostMapping("/resolve")
    @Operation(
            summary = "행정구역 계층 저장 및 조회",
            description = """
                시/도, 시/군/구, 일반구, 행정구역 정보를 계층형 Region으로 저장하거나 기존 Region을 조회합니다.
                이미 동일한 계층의 Region이 저장되어 있으면 새로 생성하지 않고 재사용합니다.
                - regionId: 요청한 계층 중 가장 하위 지역의 ID이며, 회원 지역 설정 등 다른 API에서 지역 식별에 사용됩니다.
                - regionName: 가장 하위 지역의 이름입니다.
                - fullRegionName: 시/도부터 가장 하위 지역까지 이어붙인 전체 지역명입니다.
                """
    )
    public ResponseEntity<ApiResponse<RegionResDTO.ResolveResult>> resolve(
            @Valid @RequestBody RegionReqDTO.Resolve request
    ) {
        RegionResDTO.ResolveResult response = regionService.resolve(request);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }

    @GetMapping
    @Operation(
            summary = "상위 지역 목록 조회",
            description = """
                지역 선택 화면에서 최상위 계층인 시/도 목록을 조회합니다.
                - regionList: regionId, regionName 쌍의 목록입니다.
                - regionId: 하위 지역 목록 조회(GET /api/regions/{ancestorRegionId}/children) 호출 시 경로 변수로 사용됩니다.
                """
    )
    public ApiResponse<RegionResDTO.RegionListDTO> getAncestorRegionList() {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_LIST_GET_SUCCESS, regionService.getAncestorRegionList());
    }

    @GetMapping("/{ancestorRegionId}/children")
    @Operation(
            summary = "하위 지역 목록 조회",
            description = """
                선택한 상위 지역(시/도)에 속한 하위 지역(시/군/구) 목록을 조회합니다.
                - ancestorRegionId: 상위 지역 목록 조회(GET /api/regions)에서 받은 regionId입니다.
                - regionList: regionId, regionName 쌍의 목록입니다.
                """
    )
    public ApiResponse<RegionResDTO.RegionListDTO> getDescendantRegionList(
            @Parameter(description = "하위 지역을 조회할 상위 지역 ID", example = "1")
            @PathVariable Long ancestorRegionId
    ) {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_LIST_GET_SUCCESS, regionService.getDescendantRegionList(ancestorRegionId));
    }
}
