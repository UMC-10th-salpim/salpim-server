package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.config.PasswordVerificationAttemptProperties;
import salpim.umc10thsalpim.domain.auth.entity.PasswordVerificationAttempt;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordVerificationAttemptServiceTest {

    private static final String PHONE_NUMBER = "01012345678";

    @Mock
    private PasswordVerificationAttemptRepository attemptRepository;

    @Mock
    private PasswordVerificationAttemptProperties properties;

    @InjectMocks
    private PasswordVerificationAttemptService passwordVerificationAttemptService;

    @Test
    void rejectsLockedTargetButAllowsDifferentTarget() {
        PasswordVerificationAttempt lockedAttempt = PasswordVerificationAttempt.builder()
                .purpose(PasswordVerificationPurpose.PASSWORD_RESET)
                .targetType(PasswordVerificationTargetType.PHONE_NUMBER)
                .targetValue(PHONE_NUMBER)
                .failureCount(5)
                .lockedUntil(LocalDateTime.now().plusMinutes(15))
                .build();

        given(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        )).willReturn(Optional.of(lockedAttempt));
        given(attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                "01099998888"
        )).willReturn(Optional.empty());

        assertThatThrownBy(() -> passwordVerificationAttemptService.validateAttemptAllowed(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                PHONE_NUMBER
        )).isInstanceOfSatisfying(AuthException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(AuthErrorCode.PASSWORD_VERIFICATION_ATTEMPTS_EXCEEDED));

        assertThatCode(() -> passwordVerificationAttemptService.validateAttemptAllowed(
                PasswordVerificationPurpose.PASSWORD_RESET,
                PasswordVerificationTargetType.PHONE_NUMBER,
                "01099998888"
        )).doesNotThrowAnyException();
    }

    @Test
    void recordsFailureUsingRequestedTarget() {
        given(attemptRepository.findByPurposeAndTargetTypeAndTargetValueForUpdate(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                "1"
        )).willReturn(Optional.empty());
        given(properties.getMaxFailureCount()).willReturn(5);
        given(properties.getLockDurationMillis()).willReturn(900_000L);

        passwordVerificationAttemptService.recordFailure(
                PasswordVerificationPurpose.PASSWORD_CHANGE,
                PasswordVerificationTargetType.MEMBER,
                "1"
        );

        verify(attemptRepository).save(org.mockito.ArgumentMatchers.argThat(attempt ->
                attempt.getPurpose() == PasswordVerificationPurpose.PASSWORD_CHANGE
                        && attempt.getTargetType() == PasswordVerificationTargetType.MEMBER
                        && attempt.getTargetValue().equals("1")
                        && attempt.getFailureCount() == 1
        ));
    }
}
