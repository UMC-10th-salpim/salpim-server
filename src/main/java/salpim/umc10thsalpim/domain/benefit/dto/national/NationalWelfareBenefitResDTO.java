package salpim.umc10thsalpim.domain.benefit.dto.national;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

// 중앙부처복지서비스 응답 DTO
public class NationalWelfareBenefitResDTO {

   /**
    * 목록조회 응답.
    * 지자체복지서비스 목록조회와 동일하게 header/body 래핑 없이
    * <wantedList> 루트 아래 필드가 바로 있고, <servList>가 반복되는 구조로 확인됨.
    *
    * <wantedList>
    * <totalCount/><pageNo/><numOfRows/><resultCode/><resultMessage/>
    * <servList>...</servList>
    * <servList>...</servList>
    * </wantedList>
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record NationalWelfareList(
       String resultCode,
       String resultMessage,
       String numOfRows,
       String pageNo,
       String totalCount,

       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareItem> servList
   ) {
      public boolean isSuccess() {
         return "0".equals(resultCode);
      }
   }

   // 목록조회 응답의 개별 항목(<servList>)
   // 응답 전용 타입
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record NationalWelfareItem(
       String servId,                // 서비스 ID (상세조회 키값)
       String servNm,                // 서비스명
       String servDgst,              // 서비스 요약
       String servDtlLink,           // 서비스 상세링크
       String jurMnofNm,             // 소관부처명
       String jurOrgNm,              // 소관조직명
       String srvPvsnNm,             // 제공유형
       String sprtCycNm,             // 지원주기
       String lifeArray,             // 생애주기
       String trgterIndvdlArray,     // 가구유형
       String intrsThemaArray,       // 관심주제
       String onapPsbltYn,           // 온라인신청가능여부
       String rprsCtadr,             // 문의처
       Integer inqNum,               // 조회수
       String svcfrstRegTs           // 서비스 등록일
   ) {}

   /**
    * 상세조회 응답.
    */
   @JsonRootName("wantedDtl")
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record NationalWelfareDetail(
       String resultCode,               // 결과코드
       String resultMessage,            // 결과메시지
       String servId,                   // 서비스 ID
       String servNm,                   // 서비스명
       String jurMnofNm,                // 소관부처명
       String tgtrDtlCn,                // 대상자 상세내용
       String slctCritCn,               // 선정기준 내용
       String alwServCn,                // 급여서비스 내용
       String crtrYr,                   // 기준연도
       String rprsCtadr,                // 문의처
       String wlfareInfoOutlCn,          // 서비스요약
       String sprtCycNm,                // 지원주기
       String srvPvsnNm,                // 제공유형
       String lifeArray,                // 생애주기
       String trgterIndvdlArray,        // 가구유형
       String intrsThemaArray,          // 관심주제

       // 서비스 이용 및 신청방법 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareRelatedInfo> applmetList,

       // 문의처 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareRelatedInfo> inqplCtadrList,

       // 관련 웹사이트 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareRelatedInfo> inqplHmpgReldList,

       // 서식/자료 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareRelatedInfo> basfrmList,

       // 근거법령 목록 (다른 4개와 달리 servSeDetailLink 없음)
       @JacksonXmlElementWrapper(useWrapping = false)
       List<NationalWelfareLawInfo> baslawList
   ) {
   }

   /**
    * 상세조회의 applmetList/inqplCtadrList/inqplHmpgReldList/basfrmList 4개 목록이
    * 전부 동일한 필드 구성(servSeCode/servSeDetailLink/servSeDetailNm)이라 공통 타입으로 재사용
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record NationalWelfareRelatedInfo(
       String servSeCode,           // 서비스 구분코드
       String servSeDetailLink,     // 연락처/사이트링크/서식자료링크 등
       String servSeDetailNm        // 문의처명/사이트명/서식·자료명 등
   ) {}

   /**
    * 근거법령 목록(baslawList) 전용.
    * 다른 관련정보 목록과 달리 servSeDetailLink 필드가 없음.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record NationalWelfareLawInfo(
       String servSeCode,           // 서비스 구분코드
       String servSeDetailNm        // 근거법령명
   ) {}
}
