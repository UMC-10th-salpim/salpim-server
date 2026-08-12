package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import salpim.umc10thsalpim.domain.auth.config.LoginAttemptProperties;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final String PHONE_NUMBER = "01012345678";
    private static final String CLIENT_IP = "203.0.113.10";

    @Mock
    private PasswordVerificationAttemptService attemptService;

    @Mock
    private LoginAttemptProperties properties;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    @Test
    void validatesPhoneAndIpLocksBeforeLogin() {
        loginAttemptService.validateAllowed(PHONE_NUMBER, CLIENT_IP);

        verify(attemptService).validateAttemptAllowed(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER,
                AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED
        );
        verify(attemptService).validateAttemptAllowed(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                CLIENT_IP,
                AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED
        );
    }

    @Test
    void recordsFailureWithSeparatePhoneAndIpThresholds() {
        when(properties.getPhoneMaxFailureCount()).thenReturn(5);
        when(properties.getIpMaxFailureCount()).thenReturn(15);
        when(properties.getLockDurationMillis()).thenReturn(900_000L);

        loginAttemptService.recordFailure(PHONE_NUMBER, CLIENT_IP);

        verify(attemptService).recordFailure(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER,
                5,
                900_000L
        );
        verify(attemptService).recordFailure(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                CLIENT_IP,
                15,
                900_000L
        );
    }

    @Test
    void successfulLoginClearsOnlyPhoneFailures() {
        loginAttemptService.clearPhoneFailures(PHONE_NUMBER);

        verify(attemptService).clearFailures(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        );
    }

    @Test
    void retriesWhenConcurrentRequestsCreateFirstAttemptTogether() {
        when(properties.getPhoneMaxFailureCount()).thenReturn(5);
        when(properties.getIpMaxFailureCount()).thenReturn(15);
        when(properties.getLockDurationMillis()).thenReturn(900_000L);
        doThrow(new DataIntegrityViolationException("first insert race"))
                .doNothing()
                .when(attemptService).recordFailure(
                        PasswordVerificationPurpose.LOGIN,
                        PasswordVerificationTargetType.PHONE_NUMBER,
                        PHONE_NUMBER,
                        5,
                        900_000L
                );

        loginAttemptService.recordFailure(PHONE_NUMBER, CLIENT_IP);

        verify(attemptService, times(2)).recordFailure(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER,
                5,
                900_000L
        );
    }
}
