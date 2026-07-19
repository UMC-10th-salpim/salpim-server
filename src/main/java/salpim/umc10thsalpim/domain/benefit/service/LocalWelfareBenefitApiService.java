package salpim.umc10thsalpim.domain.benefit.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import salpim.umc10thsalpim.domain.benefit.dto.local.LocalWelfareBenefitReqDTO;
import salpim.umc10thsalpim.domain.benefit.dto.local.LocalWelfareBenefitResDTO;

@Service
public class LocalWelfareBenefitApiService {

   private final RestClient welfareRestClient;

   @Value("${welfare.local.api.base-url}")
   private String baseUrl;

   @Value("${welfare.local.api.service-key}")
   private String serviceKey;

   @Value("${welfare.local.api.default-ctpv-nm:}")
   private String defaultCtpvNm;

   public LocalWelfareBenefitApiService(RestClient welfareRestClient) {
      this.welfareRestClient = welfareRestClient;
   }

   /**
    * 지자체복지서비스 목록조회.
    * RestClient가 응답 Content-Type(application/xml)을 보고
    * 자동으로 LocalWelfareBenefitResDTO.LocalWelfareList로 역직렬화한다.
    */
   public LocalWelfareBenefitResDTO.LocalWelfareList fetchWelfareList(
       LocalWelfareBenefitReqDTO.LocalWelfareList request
   ) {
      String uri = buildListUri(request);

      LocalWelfareBenefitResDTO.LocalWelfareList response = callApi(
          uri, LocalWelfareBenefitResDTO.LocalWelfareList.class);

      if (!response.isSuccess()) {
         throw new IllegalStateException(
             "지자체복지서비스 목록조회 API 오류: " + response.resultCode() + " / " + response.resultMessage());
      }
      return response;
   }

   /**
    * 지자체복지서비스 상세조회.
    * 목록조회 결과에서 얻은 servId로 지원대상, 선정기준, 신청방법 등 상세 내용을 조회한다.
    */
   public LocalWelfareBenefitResDTO.LocalWelfareDetail fetchWelfareDetail(
       LocalWelfareBenefitReqDTO.LocalWelfareDetail request
   ) {
      String uri = buildDetailUri(request);

      return callApi(uri, LocalWelfareBenefitResDTO.LocalWelfareDetail.class);
   }

   private String buildListUri(LocalWelfareBenefitReqDTO.LocalWelfareList request) {
      UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(baseUrl + "/LcgvWelfarelist")
          .queryParam("serviceKey", serviceKey)
          .queryParam("pageNo", defaultIfBlank(request.pageNo(), "1"))
          .queryParam("numOfRows", defaultIfBlank(request.numOfRows(), "10"));

      String ctpvNm = defaultIfBlank(request.ctpvNm(), defaultCtpvNm);
      if (hasText(ctpvNm)) {
         builder.queryParam("ctpvNm", ctpvNm);
      }

      if (hasText(request.searchWrd())) {
         builder.queryParam("searchWrd", request.searchWrd());
      }
      if (hasText(request.srchKeyCode())) {
         builder.queryParam("srchKeyCode", request.srchKeyCode());
      }
      if (hasText(request.lifeArray())) {
         builder.queryParam("lifeArray", request.lifeArray());
      }
      if (hasText(request.trgterIndvdlArray())) {
         builder.queryParam("trgterIndvdlArray", request.trgterIndvdlArray());
      }
      if (hasText(request.sggNm())) {
         builder.queryParam("sggNm", request.sggNm());
      }

      // serviceKey는 이미 인코딩된 키를 넣는 경우가 많아 build(true)로 재인코딩 방지
      return builder.build(true).toUriString();
   }

   private String buildDetailUri(LocalWelfareBenefitReqDTO.LocalWelfareDetail request) {
      return UriComponentsBuilder
          .fromUriString(baseUrl + "/LcgvWelfaredetailed")
          .queryParam("serviceKey", serviceKey)
          .queryParam("servId", request.servId())
          .build(true)
          .toUriString();
   }

   private <T> T callApi(String uri, Class<T> targetType) {
      try {
         return welfareRestClient.get()
             .uri(uri)
             .retrieve()
             .body(targetType);
      } catch (RestClientException e) {
         // 4xx/5xx 응답이거나, 응답 본문이 targetType으로 변환되지 않는 경우
         throw new IllegalStateException("지자체복지서비스 API 호출 실패: " + uri, e);
      }
   }

   private boolean hasText(String value) {
      return value != null && !value.isBlank();
   }

   private String defaultIfBlank(String value, String defaultValue) {
      return hasText(value) ? value : defaultValue;
   }
}
