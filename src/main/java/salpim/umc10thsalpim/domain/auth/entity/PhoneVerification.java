package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "phone_verification")
public class PhoneVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    public void updateCode(String code, LocalDateTime expiredAt) {
        this.code = code;
        this.expiredAt = expiredAt;
        this.verified = false;
    }

    public void verify() {
        this.verified = true;
    }
}
