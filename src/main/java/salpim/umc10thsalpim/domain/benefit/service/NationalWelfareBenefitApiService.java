package salpim.umc10thsalpim.domain.benefit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import salpim.umc10thsalpim.domain.benefit.dto.national.NationalWelfareBenefitReqDTO;
import salpim.umc10thsalpim.domain.benefit.dto.national.NationalWelfareBenefitResDTO;

@Service
public class NationalWelfareBenefitApiService {

   // callTp: API 자체의 필수 스위치(L: 목록, D: 상세). 오퍼레이션마다 고정.
   private static final String CALL_TYPE_LIST = "L";
   private static final String CALL_TYPE_DETAIL = "D";

   private final RestClient welfareRestClient;

   @Value("${welfare.national.api.base-url}")
   private String baseUrl;

   @Value("${welfare.national.api.service-key}")
   private String serviceKey;

   public NationalWelfareBenefitApiService(RestClient welfareRestClient) {
      this.welfareRestClient = welfareRestClient;
   }

   /**
    * 중앙부처복지서비스 목록을 조회한다.
    */
   public NationalWelfareBenefitResDTO.NationalWelfareList fetchWelfareList(
       NationalWelfareBenefitReqDTO.NationalWelfareList request
   ) {
      String uri = buildListUri(request);

      NationalWelfareBenefitResDTO.NationalWelfareList response = callApi(
          uri, NationalWelfareBenefitResDTO.NationalWelfareList.class);

      if (!response.isSuccess()) {
         throw new IllegalStateException(
             "중앙부처복지서비스 목록조회 API 오류: " + response.resultCode() + " / " + response.resultMessage());
      }
      return response;
   }

   /**
    * 중앙부처복지서비스 상세조회.
   */
   public NationalWelfareBenefitResDTO.NationalWelfareDetail fetchWelfareDetail(
       NationalWelfareBenefitReqDTO.NationalWelfareDetail request
   ) {
      String uri = buildDetailUri(request);

      return callApi(uri, NationalWelfareBenefitResDTO.NationalWelfareDetail.class);
   }

   private String buildListUri(NationalWelfareBenefitReqDTO.NationalWelfareList request) {
      UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(baseUrl + "/NationalWelfarelistV001")
          .queryParam("serviceKey", serviceKey)
          .queryParam("callTp", CALL_TYPE_LIST)
          .queryParam("pageNo", defaultIfBlank(request.pageNo(), "1"))
          .queryParam("numOfRows", defaultIfBlank(request.numOfRows(), "10"))
          .queryParam("srchKeyCode", defaultIfBlank(request.srchKeyCode(), "003")); // 003: 제목+내용

      if (hasText(request.searchWrd())) {
         builder.queryParam("searchWrd", request.searchWrd());
      }
      if (hasText(request.lifeArray())) {
         builder.queryParam("lifeArray", request.lifeArray());
      }
      if (hasText(request.trgterIndvdlArray())) {
         builder.queryParam("trgterIndvdlArray", request.trgterIndvdlArray());
      }
      if (hasText(request.intrsThemaArray())) {
         builder.queryParam("intrsThemaArray", request.intrsThemaArray());
      }
      if (hasText(request.age())) {
         builder.queryParam("age", request.age());
      }
      if (hasText(request.onapPsbltYn())) {
         builder.queryParam("onapPsbltYn", request.onapPsbltYn());
      }
      if (hasText(request.orderBy())) {
         builder.queryParam("orderBy", request.orderBy());
      }

      // serviceKey는 이미 인코딩된 키를 넣는 경우가 많아 build(true)로 재인코딩 방지
      return builder.build(true).toUriString();
   }

   private String buildDetailUri(NationalWelfareBenefitReqDTO.NationalWelfareDetail request) {
      return UriComponentsBuilder
          .fromUriString(baseUrl + "/NationalWelfaredetailedV001")
          .queryParam("serviceKey", serviceKey)
          .queryParam("callTp", CALL_TYPE_DETAIL)
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
         throw new IllegalStateException("중앙부처복지서비스 API 호출 실패: " + uri, e);
      }
   }

   private boolean hasText(String value) {
      return value != null && !value.isBlank();
   }

   private String defaultIfBlank(String value, String defaultValue) {
      return hasText(value) ? value : defaultValue;
   }
}
