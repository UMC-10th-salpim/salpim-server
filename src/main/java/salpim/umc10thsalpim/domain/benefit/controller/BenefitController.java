package salpim.umc10thsalpim.domain.benefit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.exception.code.BenefitSuccessCode;
import salpim.umc10thsalpim.domain.benefit.service.BenefitService;
import salpim.umc10thsalpim.global.apiPayload.ApiResponse;
import salpim.umc10thsalpim.global.apiPayload.code.BaseSuccessCode;
import salpim.umc10thsalpim.global.dto.CursorResDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.net.URI;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/benefits")
@Tag(name = "혜택", description = "복지 혜택 조회 및 신청 지원 API")
public class BenefitController {

    private final BenefitService benefitService;

    //직접 찾기 검색 api
    @GetMapping("/search")
    public ApiResponse<CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO>> getSearchResult
    (
            @RequestParam(required = false, defaultValue = "")
            String searchKey,
            @RequestParam List<Long> regionIds, //필수
            @RequestParam(required = false) List<Long> categoryIds, //null -> 모든 카테고리
            @RequestParam(name="cursor", defaultValue = "-1") String cursor,
            @RequestParam(name="pageSize", defaultValue = "10") @Positive Integer pageSize,
            @RequestParam(name="sort", defaultValue = "popular") String sort
    ) {
        return ApiResponse.onSuccess(BenefitSuccessCode.BENEFIT_VIEW, benefitService.getSearchResult(searchKey, regionIds, categoryIds, cursor, pageSize, sort));
    }

    @GetMapping("/{benefitId}/application-helper")
    @Operation(
            summary = "신청 도우미 조회",
            description = """
                복지 혜택의 신청 방식, 온라인 신청 가능 여부, 신청 기간과 URL을 조회합니다.
                회원의 지역 및 연령 조건 충족 여부를 함께 제공합니다.
                연령 조건을 확인할 수 없는 경우 isAgeSatisfied는 null로 반환됩니다.
                """
    )
    public ApiResponse<BenefitResDTO.GetApplicationHelperInfo> getApplicationHelperInfoApiResponse(
            @Parameter(description = "조회할 복지 혜택 ID", example = "1")
            @PathVariable Long benefitId
            ){
        BaseSuccessCode code = BenefitSuccessCode.BENEFIT_VIEW;

        Long memberId = 1L; //TODO : get memberId from accessToken

        BenefitResDTO.GetApplicationHelperInfo response =
                benefitService.getApplicationHelperInfo(memberId, benefitId);
        return ApiResponse.onSuccess(code, response);

    }

    @GetMapping("/{benefitId}/application-link")
    @Operation(
            summary = "온라인 신청 사이트로 이동",
            description = "온라인 신청이 가능한 복지 혜택의 신청 사이트로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "온라인 신청 사이트 리다이렉트 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "온라인 신청을 지원하지 않는 헤택"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "복지 혜택 또는 신청 규칙을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "신청 URL이 없거나 형식이 올바르지 않음"
            )
    })
    public ResponseEntity<Void> redirectToOnlineApplication(
            @Parameter(description = "온라인 신청할 복지 혜택 ID", example = "1")
            @PathVariable Long benefitId
    ) {
        String applicationUrl = benefitService.getOnlineApplicationUrl(benefitId);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(applicationUrl))
                .build();
    }
}
