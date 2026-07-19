package salpim.umc10thsalpim.domain.benefit.dto.local;

// 지자체복지서비스 요청 DTO
public class LocalWelfareBenefitReqDTO {

    /**
     * 목록조회 요청
     * pageNo: 페이지 번호
     * numOfRows: 한 페이지 결과 수
     * searchWrd: 검색어
     * srchKeyCode: 검색조건 코드
     * lifeArray: 생애주기 코드
     * trgterIndvdlArray: 대상특성 코드
     * ctpvNm: 시도명
     * sggNm: 시군구명
     * */
    public record LocalWelfareList(
        String pageNo,
        String numOfRows,
        String searchWrd,
        String srchKeyCode,
        String lifeArray,
        String trgterIndvdlArray,
        String ctpvnM,
        String sggNm
    ) {}

   /**
    * 상세조회 요청
    * servId: 서비스 id (목록조회 결과에서 얻은 값, 필수)
    * */
   public record LocalWelfareDetail(
      String servId
   ){}
}
