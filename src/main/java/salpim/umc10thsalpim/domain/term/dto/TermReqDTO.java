package salpim.umc10thsalpim.domain.term.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class TermReqDTO {

    public record SubmitAgreements(
            @NotEmpty(message = "동의할 약관 목록은 필수입니다.")
            @Valid
            List<AgreementItem> agreements
    ) {}

    public record AgreementItem(
            @NotNull(message = "약관 버전 ID는 필수입니다.")
            @Positive(message = "약관 버전 ID는 양수여야 합니다.")
            Long termsVersionId,

            @NotNull(message = "동의 여부는 필수입니다.")
            Boolean agreed
    ) {}
}
