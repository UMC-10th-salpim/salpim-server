package salpim.umc10thsalpim.domain.benefit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocalWelfareBenefit extends BaseEntity {

   @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 20)
   private String servId;          // 복지로 서비스 ID (상세조회 키값)

   @Column(nullable = false, length = 200)
   private String servNm;          // 서비스명

   @Lob @Column(columnDefinition = "TEXT")
   private String servDgst;        // 서비스 요약

   @Column(length = 500)
   private String servDtlLink;     // 복지로 상세페이지 링크

   @Column(length = 50)
   private String ctpvNm;          // 시도명

   @Column(length = 50)
   private String sggNm;           // 시군구명

   @Column(length = 100)
   private String bizChrDeptNm;    // 사업 담당 부서명

   @Column(length = 100)
   private String srvPvsnNm;       // 서비스 제공 방법명

   @Column(length = 100)
   private String aplyMtdNm;       // 신청 방법명

   @Column(length = 100)
   private String sprtCycNm;       // 지원 주기명

   @Column(length = 200)
   private String lifeNmArray;     // 생애주기명

   @Column(length = 200)
   private String trgterIndvdlNmArray; // 대상특성명

   @Column(length = 200)
   private String intrsThemaNmArray;   // 관심주제명

   private Integer inqNum;         // 조회수

   @Column(length = 20)
   private String lastModYmd;      // API상 최종수정일 (yyyyMMdd 문자열 그대로 저장)
}
