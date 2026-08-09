package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.config.PasswordVerificationAttemptProperties;
import salpim.umc10thsalpim.domain.auth.entity.PasswordVerificationAttempt;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordVerificationAttemptService {

    private final PasswordVerificationAttemptRepository attemptRepository;
    private final PasswordVerificationAttemptProperties properties;

    @Transactional(readOnly = true)
    public void validateAttemptAllowed(
            PasswordVerificationPurpose purpose,
            PasswordVerificationTargetType targetType,
            String targetValue
    ) {
        attemptRepository.findByPurposeAndTargetTypeAndTargetValue(
                purpose,
                targetType,
                targetValue
        ).ifPresent(attempt -> {
            if(attempt.isLocked(LocalDateTime.now())) {
                throw new AuthException(
                        AuthErrorCode.PASSWORD_VERIFICATION_ATTEMPTS_EXCEEDED
                );
            }
        });
    }

    @Transactional
    public void recordFailure(
            PasswordVerificationPurpose purpose,
            PasswordVerificationTargetType targetType,
            String targetValue
    ) {
        LocalDateTime now = LocalDateTime.now();

        PasswordVerificationAttempt attempt = attemptRepository
                .findByPurposeAndTargetTypeAndTargetValueForUpdate(
                        purpose,
                        targetType,
                        targetValue
        ).orElseGet(() -> PasswordVerificationAttempt.create(
                        purpose,
                        targetType,
                        targetValue
                ));

        attempt.recordFailure(
                now,
                properties.getMaxFailureCount(),
                properties.getLockDurationMillis()
        );

        attemptRepository.save(attempt);
    }

    @Transactional
    public void clearFailures(
            PasswordVerificationPurpose purpose,
            PasswordVerificationTargetType targetType,
            String targetValue
    ) {
        attemptRepository.findByPurposeAndTargetTypeAndTargetValueForUpdate(
                purpose,
                targetType,
                targetValue
        ).ifPresent(PasswordVerificationAttempt::clearFailures);
    }
}
