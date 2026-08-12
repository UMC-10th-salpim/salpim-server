package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import salpim.umc10thsalpim.domain.auth.config.LoginAttemptProperties;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final PasswordVerificationPurpose PURPOSE = PasswordVerificationPurpose.LOGIN;

    private final PasswordVerificationAttemptService attemptService;
    private final LoginAttemptProperties properties;

    public void validateAllowed(String phoneNumber, String clientIp) {
        attemptService.validateAttemptAllowed(
                PURPOSE,
                PasswordVerificationTargetType.PHONE_NUMBER,
                phoneNumber,
                AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED
        );
        attemptService.validateAttemptAllowed(
                PURPOSE,
                PasswordVerificationTargetType.IP_ADDRESS,
                clientIp,
                AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED
        );
    }

    public void recordFailure(String phoneNumber, String clientIp) {
        recordFailureWithFirstInsertRetry(
                PasswordVerificationTargetType.PHONE_NUMBER,
                phoneNumber,
                properties.getPhoneMaxFailureCount(),
                properties.getPhoneLockDurationMillis()
        );
        recordFailureWithFirstInsertRetry(
                PasswordVerificationTargetType.IP_ADDRESS,
                clientIp,
                properties.getIpMaxFailureCount(),
                properties.getIpLockDurationMillis()
        );
    }

    public void clearFailures(String phoneNumber, String clientIp) {
        attemptService.clearFailures(
                PURPOSE,
                PasswordVerificationTargetType.PHONE_NUMBER,
                phoneNumber
        );
        attemptService.clearFailures(
                PURPOSE,
                PasswordVerificationTargetType.IP_ADDRESS,
                clientIp
        );
    }

    private void recordFailureWithFirstInsertRetry(
            PasswordVerificationTargetType targetType,
            String targetValue,
            int maxFailureCount,
            long lockDurationMillis
    ) {
        try {
            attemptService.recordFailure(
                    PURPOSE,
                    targetType,
                    targetValue,
                    maxFailureCount,
                    lockDurationMillis
            );
        } catch (DataIntegrityViolationException firstInsertRace) {
            attemptService.recordFailure(
                    PURPOSE,
                    targetType,
                    targetValue,
                    maxFailureCount,
                    lockDurationMillis
            );
        }
    }
}
