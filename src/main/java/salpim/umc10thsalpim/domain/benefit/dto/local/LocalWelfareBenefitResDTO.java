package salpim.umc10thsalpim.domain.benefit.dto.local;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

// 지자체복지서비스 응답 DTO

/**
 * <wantedList>
 * <resultCode>0</resultCode>
 * <resultMessage>SUCCESS</resultMessage>
 * <numOfRows>10</numOfRows>
 * <pageNo>1</pageNo>
 * <totalCount>4559</totalCount>
 * <servList>...</servList>   (건수만큼 반복되는 sibling 태그)
 * <servList>...</servList>
 * </wantedList>
 */
public class LocalWelfareBenefitResDTO {

   // 목록조회 응답
   // XML에 정의되지 않은 필드가 더 있어도 에러 없이 무시하기 위함
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record LocalWelfareList(
       String resultCode,
       String resultMessage,
       String numOfRows,
       String pageNo,
       String totalCount,
       @JacksonXmlElementWrapper(useWrapping = false)
       List<LocalWelfareItem> servList
   ) {}

   // 상세조회 응답
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record LocalWelfareDetail(
       String resultCode,               // 결과코드
       String resultMessage,            // 결과메시지
       String servId,                   // 서비스 ID
       String servNm,                   // 서비스명
       String enfcBgngYmd,              // 시행시작일자
       String enfcEndYmd,               // 시행종료일자
       String bizChrDeptNm,             // 사업담당부서명
       String ctpvNm,                   // 시도명
       String sggNm,                    // 시군구명
       String servDgst,                 // 서비스 요약
       String lifeNmArray,              // 생애주기명
       String trgterIndvdlNmArray,      // 가구상황명
       String intrsThemaNmArray,        // 관심주제명
       String sprtCycNm,                // 지원주기명
       String srvPvsnNm,                // 제공유형명
       String aplyMtdNm,                // 신청방법명
       String sprtTrgtCn,               // 지원대상 내용
       String slctCritCn,               // 선정기준 내용
       String alwServCn,                // 급여서비스 내용
       String aplyMtdCn,                // 신청방법 내용
       Integer inqNum,                  // 조회수
       String lastModYmd,               // 최종수정일자

       // 문의처 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<LocalWelfareRelatedInfo> inqplCtadrList,

       // 관련 웹사이트 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<LocalWelfareRelatedInfo> inqplHmpgReldList,

       // 근거법령 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<LocalWelfareRelatedInfo> baslawList,

       // 서식/자료 목록
       @JacksonXmlElementWrapper(useWrapping = false)
       List<LocalWelfareRelatedInfo> basfrmList
   ) {}

   // 목록조회 응답의 개별 항목(<servList>)
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record LocalWelfareItem(
       String servId,               // 서비스 ID (상세조회 키값)
       String servNm,               // 서비스명
       String servDgst,             // 서비스 요약
       String servDtlLink,          // 복지로 상세페이지 링크
       String ctpvNm,               // 시도명
       String sggNm,                // 시군구명
       String bizChrDeptNm,         // 사업 담당 부서명
       String srvPvsnNm,            // 서비스 제공 방법명
       String aplyMtdNm,            // 신청 방법명
       String sprtCycNm,            // 지원 주기명
       String lifeNmArray,          // 생애주기명
       String trgterIndvdlNmArray,  // 대상특성명
       String intrsThemaNmArray,    // 관심주제명
       Integer inqNum,              // 조회수
       String lastModYmd            // 최종수정일 (yyyyMMdd 문자열)
   ) {}

   // 상세조회의 문의처/관련웹사이트/근거법령/서식자료 목록이
   // 동일한 필드 구성이라 공통 타입으로 재사용
   @JsonIgnoreProperties(ignoreUnknown = true)
   public record LocalWelfareRelatedInfo(
       String wlfareInfoReldNm,     // 복지정보관련명
       String wlfareInfoReldCn,     // 복지정보관련내용
       String wlfareInfoDtlCd       // 복지정보상세코드
   ) {}
}
