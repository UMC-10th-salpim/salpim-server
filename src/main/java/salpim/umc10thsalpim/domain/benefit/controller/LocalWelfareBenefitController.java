package salpim.umc10thsalpim.domain.benefit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salpim.umc10thsalpim.domain.benefit.dto.local.LocalWelfareBenefitReqDTO;
import salpim.umc10thsalpim.domain.benefit.dto.local.LocalWelfareBenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.service.LocalWelfareBenefitApiService;

@RestController
@RequestMapping("/api/welfare/local")
public class LocalWelfareBenefitController {

   private final LocalWelfareBenefitApiService localWelfareBenefitApiService;

   @Autowired
   public LocalWelfareBenefitController(LocalWelfareBenefitApiService localWelfareBenefitApiService) {
      this.localWelfareBenefitApiService = localWelfareBenefitApiService;
   }

   @GetMapping("/list")
    public LocalWelfareBenefitResDTO.LocalWelfareList getWelfareList(
            @ModelAttribute LocalWelfareBenefitReqDTO.LocalWelfareList request
    ) {
        return localWelfareBenefitApiService.fetchWelfareList(request);
    }

    /**
     * 목록조회에서 얻은 servId로 상세 내용을 조회.
     * 예: GET /api/welfare/local/detail?servId=00000014775
     */
    @GetMapping("/detail")
    public LocalWelfareBenefitResDTO.LocalWelfareDetail getWelfareDetail(
            @ModelAttribute LocalWelfareBenefitReqDTO.LocalWelfareDetail request
    ) {
        return localWelfareBenefitApiService.fetchWelfareDetail(request);
    }
}
