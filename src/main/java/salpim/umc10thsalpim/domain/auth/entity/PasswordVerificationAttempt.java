package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.Duration;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "password_verification_attempt",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_password_verification_purpose_type_value",
                columnNames = {"purpose", "target_type", "target_value"}
        )
)
public class PasswordVerificationAttempt extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "purpose")
        @Enumerated(EnumType.STRING)
        private PasswordVerificationPurpose purpose;

        @Column(name = "target_type")
        @Enumerated(EnumType.STRING)
        private PasswordVerificationTargetType targetType;

        @Column(name = "target_value")
        private String targetValue;

        @Column(name = "failure_count")
        private int failureCount;

        @Column(name = "last_failed_at")
        private LocalDateTime lastFailedAt;

        @Column(name = "locked_until")
        private LocalDateTime lockedUntil;

        public static PasswordVerificationAttempt create(
                PasswordVerificationPurpose purpose,
                PasswordVerificationTargetType targetType,
                String targetValue
        ) {
                return PasswordVerificationAttempt.builder()
                        .purpose(purpose)
                        .targetType(targetType)
                        .targetValue(targetValue)
                        .failureCount(0)
                        .build();
        }

        public boolean isLocked(LocalDateTime now){
                return lockedUntil != null && now.isBefore(lockedUntil);
        }

        public void recordFailure(
                LocalDateTime now,
                int maxFailureCount,
                long lockDurationMillis
        ){
                if(lockedUntil != null && !now.isBefore(lockedUntil)) {
                        clearFailures();
                }

                failureCount++;
                lastFailedAt = now;

                if(failureCount >= maxFailureCount) {
                        lockedUntil = now.plus(Duration.ofMillis(lockDurationMillis));
                }
        }

        public void clearFailures() {
                failureCount = 0;
                lastFailedAt = null;
                lockedUntil = null;
        }
}
