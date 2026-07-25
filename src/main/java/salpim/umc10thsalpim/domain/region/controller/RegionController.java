package salpim.umc10thsalpim.domain.region.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.service.RegionService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.GeneralSuccessCode;

@Tag(name = "Region", description = "Region API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/regions")
public class RegionController {

    private final RegionService regionService;

    @Operation(
            summary = "행정구역 계층 저장 및 조회",
            description = "시·도, 시, 구·군, 읍·면·동 정보를 계층형 Region으로 저장하거나 기존 Region을 조회한 후 가장 하위 지역 ID를 반환합니다."
    )
    @PostMapping("/resolve")
    public ResponseEntity<ApiResponse<RegionResDTO.ResolveResult>> resolve(
            @Valid @RequestBody RegionReqDTO.Resolve request
    ) {
        RegionResDTO.ResolveResult response = regionService.resolve(request);
        return ResponseEntity.status(GeneralSuccessCode.OK.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}
