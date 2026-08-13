package salpim.umc10thsalpim.domain.auth.entity;

import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordVerificationAttemptTest {

    private static final int MAX_FAILURE_COUNT = 5;
    private static final long LOCK_DURATION_MILLIS = 15 * 60 * 1000L;
    private static final long IP_LOCK_DURATION_MILLIS = 60 * 60 * 1000L;

    @Test
    void locksAfterMaximumFailures() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 12, 0);
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                "01012345678"
        );

        for (int failure = 0; failure < MAX_FAILURE_COUNT; failure++) {
            attempt.recordFailure(now, MAX_FAILURE_COUNT, LOCK_DURATION_MILLIS);
        }

        assertThat(attempt.getFailureCount()).isEqualTo(MAX_FAILURE_COUNT);
        assertThat(attempt.isLocked(now.plusMinutes(14))).isTrue();
        assertThat(attempt.isLocked(now.plusMinutes(15))).isFalse();
    }

    @Test
    void startsNewFailureCycleAfterLockExpires() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 12, 0);
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                "1"
        );

        for (int failure = 0; failure < MAX_FAILURE_COUNT; failure++) {
            attempt.recordFailure(now, MAX_FAILURE_COUNT, LOCK_DURATION_MILLIS);
        }

        attempt.recordFailure(now.plusMinutes(15), MAX_FAILURE_COUNT, LOCK_DURATION_MILLIS);

        assertThat(attempt.getFailureCount()).isEqualTo(1);
        assertThat(attempt.getLockedUntil()).isNull();
    }

    @Test
    void clearsFailuresAfterSuccessfulVerification() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 12, 0);
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                "1"
        );
        attempt.recordFailure(now, MAX_FAILURE_COUNT, LOCK_DURATION_MILLIS);

        attempt.clearFailures();

        assertThat(attempt.getFailureCount()).isZero();
        assertThat(attempt.getLastFailedAt()).isNull();
        assertThat(attempt.getLockedUntil()).isNull();
    }

    @Test
    void keepsCountingConsecutiveFailuresAfterTimePasses() {
        LocalDateTime now = LocalDateTime.now();
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                "203.0.113.10"
        );

        attempt.recordFailure(now, 15, LOCK_DURATION_MILLIS);
        attempt.recordFailure(now.plusMinutes(15), 15, LOCK_DURATION_MILLIS);

        assertThat(attempt.getFailureCount()).isEqualTo(2);
        assertThat(attempt.getLockedUntil()).isNull();
    }

    @Test
    void locksIpAfterFifteenConsecutiveFailuresEvenWhenFailuresAreSpreadOut() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 12, 0);
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                "203.0.113.10"
        );

        for (int failure = 0; failure < 15; failure++) {
            attempt.recordFailure(now.plusMinutes(failure * 20L), 15, IP_LOCK_DURATION_MILLIS);
        }

        LocalDateTime fifteenthFailureAt = now.plusMinutes(14 * 20L);
        assertThat(attempt.getFailureCount()).isEqualTo(15);
        assertThat(attempt.isLocked(fifteenthFailureAt.plusMinutes(59))).isTrue();
        assertThat(attempt.isLocked(fifteenthFailureAt.plusMinutes(60))).isFalse();
    }
}
