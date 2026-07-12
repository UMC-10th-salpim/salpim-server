package salpim.umc10thsalpim.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_phone_number", columnNames = "phone_number"),
                @UniqueConstraint(name = "uk_member_login_type_kakao_id", columnNames = {"login_type", "kakao_id"})
        })
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialProvider loginType;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password")
    private String password;

    @Column(name = "kakao_id")
    private String kakaoId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "password_recovery_answer", nullable = false)
    private String passwordRecoveryAnswer;

    @Column(name = "welfare_center")
    private String welfareCenter;
}
