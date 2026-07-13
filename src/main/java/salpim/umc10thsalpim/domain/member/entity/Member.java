package salpim.umc10thsalpim.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type")
    private SocialProvider loginType; // SocialProvider ENUM 필요

    @Column(name = "phone_number")
    private String phoneNumber;

    private String password;

    @Column(name = "kakao_id")
    private String kakaoId;

    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender; // Gender ENUM 필요

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    // 연관관계 매핑 (지연 로딩 적용)
    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "region_id", nullable = false)
    //private Region region;

    // 💡 참고: 제공해주신 ERD 원본에는 없지만, 방금 전 우리가 카카오맵 시설 매칭 로직을 위해
    // 추가하기로 논의했던 '관할 행정동(hemdNm)' 컬럼을 포함해 두었습니다.
    @Column(name = "jurisdiction_center", length = 50)
    private String serviceCenter;
}
