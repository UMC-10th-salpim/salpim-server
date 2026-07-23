package salpim.umc10thsalpim.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.region.entity.Region;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "region_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_region")
    )
    private Region region;

    @Column(name = "region_id", insertable = false, updatable = false)
    private Long regionId;

    @Column(name = "password_recovery_answer")
    private String passwordRecoveryAnswer;

    @Column(name = "welfare_center")
    private String welfareCenter;

    @PrePersist
    @PreUpdate
    public void validateLoginTypeFields() {
        if (loginType == SocialProvider.LOCAL) {
            validateLocalMemberFields();
        }
        if (loginType == SocialProvider.KAKAO) {
            validateKakaoMemberFields();
        }
    }

    private void validateLocalMemberFields() {
        if (isBlank(password)) {
            throw new MemberException(MemberErrorCode.REQUIRED_LOCAL_PASSWORD);
        }
        if (isBlank(passwordRecoveryAnswer)) {
            throw new MemberException(MemberErrorCode.REQUIRED_PASSWORD_RECOVERY_ANSWER);
        }
    }

    private void validateKakaoMemberFields() {
        if (isBlank(kakaoId)) {
            throw new MemberException(MemberErrorCode.REQUIRED_KAKAO_ID);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public Long getRegionId() {
        return region != null ? region.getId() : regionId;
    }

    public void updateProfile(
            String name,
            LocalDate birthDate,
            Gender gender,
            String roadAddress,
            String detailAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            Region region
    ) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
