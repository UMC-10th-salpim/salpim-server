package salpim.umc10thsalpim.domain.term.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TermReqDTOTest {

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
    void submitAgreementsRejectsNullAgreementItem() {
        TermReqDTO.SubmitAgreements request = new TermReqDTO.SubmitAgreements(
                Arrays.asList(new TermReqDTO.AgreementItem(1L, true), null)
        );

        Set<ConstraintViolation<TermReqDTO.SubmitAgreements>> violations = validator.validate(request);

        assertThat(violations)
                .anySatisfy(violation ->
                        assertThat(violation.getMessage()).isEqualTo("약관 동의 항목은 null일 수 없습니다."));
    }
}
