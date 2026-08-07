package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "phone_verification",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_phone_verification_phone_purpose",
                columnNames = {"phone_number", "purpose"}
        )
)
public class PhoneVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "code", nullable = false, length = 100)
    private String codeHash;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private PhoneVerificationPurpose purpose;

    @Column(name = "verification_token_hash", length = 255)
    private String verificationTokenHash;

    @Column(name = "token_expired_at")
    private LocalDateTime tokenExpiredAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    public void updateCode(
            String codeHash,
            LocalDateTime expiredAt,
            LocalDateTime sentAt
    ) {
        this.codeHash = codeHash;
        this.expiredAt = expiredAt;
        this.sentAt = sentAt;
        this.verified = false;
        this.failedAttempts = 0;
        this.lockedUntil = null;
        this.verificationTokenHash = null;
        this.tokenExpiredAt = null;
        this.usedAt = null;
    }

    public void verify() {
        this.verified = true;
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    public void verifyAndIssueToken(
            String verificationTokenHash,
            LocalDateTime tokenExpiredAt
    ) {
        this.verified = true;
        this.verificationTokenHash = verificationTokenHash;
        this.tokenExpiredAt = tokenExpiredAt;
        this.usedAt = null;
    }

    public void consumeVerificationToken() {
        this.usedAt = LocalDateTime.now();
    }

    public boolean isLockedAt(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void recordFailedAttempt(int maxAttempts, LocalDateTime lockedUntil) {
        this.failedAttempts++;
        if (this.failedAttempts >= maxAttempts) {
            this.lockedUntil = lockedUntil;
        }
    }

}
