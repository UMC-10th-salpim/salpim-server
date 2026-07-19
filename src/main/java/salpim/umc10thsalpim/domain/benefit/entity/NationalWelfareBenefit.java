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
public class NationalWelfareBenefit extends BaseEntity {

   @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 20)
   private String servId;          // 복지로 서비스 ID (상세조회 키값)

   @Column(nullable = false, length = 200)
   private String servNm;          // 서비스명

   @Lob
   @Column(columnDefinition = "TEXT")
   private String servDgst;        // 서비스 요약

   @Column(length = 500)
   private String servDtlLink;     // 서비스 상세링크

   @Column(length = 100)
   private String jurMnofNm;       // 소관부처명

   @Column(length = 100)
   private String jurOrgNm;        // 소관조직명

   @Column(length = 100)
   private String srvPvsnNm;       // 제공유형

   @Column(length = 100)
   private String sprtCycNm;       // 지원주기

   @Column(length = 200)
   private String lifeArray;       // 생애주기

   @Column(length = 200)
   private String trgterIndvdlArray; // 가구유형

   @Column(length = 200)
   private String intrsThemaArray; // 관심주제

   @Column(length = 5)
   private String onapPsbltYn;     // 온라인신청가능여부

   @Column(length = 200)
   private String rprsCtadr;       // 문의처

   private Integer inqNum;         // 조회수

   @Column(length = 30)
   private String svcfrstRegTs;    // 서비스 등록일
}
