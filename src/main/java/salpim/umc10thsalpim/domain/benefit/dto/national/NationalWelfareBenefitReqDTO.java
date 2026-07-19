package salpim.umc10thsalpim.domain.benefit.dto.national;

// 중앙복지서비스 요청 DTO
public class NationalWelfareBenefitReqDTO {

   /**
    * 목록조회 요청
    * pageNo: 검색 시작위치 (기본 1, 최대 1000)
    * numOfRows: 출력건수 (기본 10, 최대 500)
    * srchKeyCode: 검색조건 코드 (001 제목 / 002 내용 / 003 제목+내용, 필수)
    * searchWrd: 검색어
    * lifeArray: 생애주기 코드
    * trgterIndvdlArray: 가구유형 코드
    * intrsThemaArray: 관심주제 코드
    * age: 나이
    * onapPsbltYn: 온라인신청 가능여부 (Y/N)
    * orderBy: 정렬순서 (date: 조회순, popular: 인기순)
    */
   public record NationalWelfareList(
       String pageNo,
       String numOfRows,
       String srchKeyCode,
       String searchWrd,
       String lifeArray,
       String trgterIndvdlArray,
       String intrsThemaArray,
       String age,
       String onapPsbltYn,
       String orderBy
   ) {}

   // 상세조회 요청
   // servId: 서비스 ID (목록조회 결과에서 얻은 값, 필수)
   public record NationalWelfareDetail(
       String servId
   ) {}
}
