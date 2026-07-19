package salpim.umc10thsalpim.domain.benefit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.benefit.dto.national.NationalWelfareBenefitReqDTO;
import salpim.umc10thsalpim.domain.benefit.dto.national.NationalWelfareBenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.service.NationalWelfareBenefitApiService;

@RestController
@RequestMapping("/api/welfare/national")
public class NationalWelfareBenefitController {

   private final NationalWelfareBenefitApiService nationalWelfareBenefitApiService;

   public NationalWelfareBenefitController(NationalWelfareBenefitApiService nationalWelfareBenefitApiService) {
      this.nationalWelfareBenefitApiService = nationalWelfareBenefitApiService;
   }

   /**
     * 프론트엔드에서 검색 조건(query string)을 받아 중앙부처복지서비스 목록을 조회
     * 현재는 조회 결과(DTO)를 그대로 반환. DB 저장(엔티티 매핑)은 아직 미구현
     */
    @GetMapping("/list")
    public NationalWelfareBenefitResDTO.NationalWelfareList getWelfareList(
            @ModelAttribute NationalWelfareBenefitReqDTO.NationalWelfareList request
    ) {
        return nationalWelfareBenefitApiService.fetchWelfareList(request);
    }

    /**
     * 목록조회에서 얻은 servId로 상세 내용을 조회
     */
    @GetMapping("/detail")
    public NationalWelfareBenefitResDTO.NationalWelfareDetail getWelfareDetail(
            @ModelAttribute NationalWelfareBenefitReqDTO.NationalWelfareDetail request
    ) {
        return nationalWelfareBenefitApiService.fetchWelfareDetail(request);
    }
}
