package salpim.umc10thsalpim.domain.auth.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;

final class MemberConstraintViolationClassifier {

    static final String PHONE_NUMBER_CONSTRAINT = "uk_member_phone_number";
    static final String KAKAO_ACCOUNT_CONSTRAINT = "uk_member_login_type_kakao_id";

    private MemberConstraintViolationClassifier() {
    }

    static boolean isViolationOf(
            DataIntegrityViolationException exception,
            String constraintName
    ) {
        String normalizedConstraintName = constraintName.toLowerCase(Locale.ROOT);
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation
                    && constraintViolation.getConstraintName() != null
                    && constraintName.equalsIgnoreCase(constraintViolation.getConstraintName())) {
                return true;
            }
            if (current.getMessage() != null
                    && current.getMessage().toLowerCase(Locale.ROOT)
                    .contains(normalizedConstraintName)) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return false;
    }
}
