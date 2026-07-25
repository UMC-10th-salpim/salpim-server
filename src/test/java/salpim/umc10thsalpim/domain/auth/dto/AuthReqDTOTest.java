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
    void localSignupAcceptsSixDigitPassword() {
        AuthReqDTO.LocalSignup request = localSignup("123456");

        Set<ConstraintViolation<AuthReqDTO.LocalSignup>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void localSignupRejectsPasswordThatIsNotSixDigits() {
        AuthReqDTO.LocalSignup request = localSignup("qwer1234");

        Set<ConstraintViolation<AuthReqDTO.LocalSignup>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath()).hasToString("password");
                    assertThat(violation.getMessage()).isEqualTo("password must be exactly 6 digits.");
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
