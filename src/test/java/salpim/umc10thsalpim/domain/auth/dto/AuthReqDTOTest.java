package salpim.umc10thsalpim.domain.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import salpim.umc10thsalpim.domain.member.enums.Gender;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthReqDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void localSignupAcceptsStrongPassword() {
        AuthReqDTO.LocalSignup request = localSignup("Salpim123!");

        Set<ConstraintViolation<AuthReqDTO.LocalSignup>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void localSignupRejectsWeakPassword() {
        AuthReqDTO.LocalSignup request = localSignup("123456");

        Set<ConstraintViolation<AuthReqDTO.LocalSignup>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath()).hasToString("password");
                    assertThat(violation.getMessage()).isEqualTo(
                            "password must be 8-64 characters and include a letter, number, and special character."
                    );
                });
    }

    @Test
    void passwordResetAcceptsSixDigitPassword() {
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset("reset-token", "123456");

        Set<ConstraintViolation<AuthReqDTO.PasswordReset>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void passwordResetRejectsPasswordThatIsNotSixDigits() {
        AuthReqDTO.PasswordReset request = new AuthReqDTO.PasswordReset("reset-token", "12345");

        Set<ConstraintViolation<AuthReqDTO.PasswordReset>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath()).hasToString("newPassword");
                    assertThat(violation.getMessage()).isEqualTo("비밀번호는 6자리 숫자여야 합니다.");
                });
    }

    private AuthReqDTO.LocalSignup localSignup(String password) {
        return new AuthReqDTO.LocalSignup(
                "Jihong",
                LocalDate.of(2002, 3, 11),
                Gender.MALE,
                "010-3176-8867",
                password,
                "Goyang Deogyang Hwarang-ro 28",
                "B",
                37.1234567,
                126.1234567,
                1L,
                "Seoul"
        );
    }
}
