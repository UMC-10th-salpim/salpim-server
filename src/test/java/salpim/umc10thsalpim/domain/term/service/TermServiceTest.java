package salpim.umc10thsalpim.domain.term.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.term.dto.TermReqDTO;
import salpim.umc10thsalpim.domain.term.exception.AgreementException;
import salpim.umc10thsalpim.domain.term.exception.code.AgreementErrorCode;
import salpim.umc10thsalpim.domain.term.repository.MemberTermAgreementRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsClauseRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsTypeRepository;
import salpim.umc10thsalpim.domain.term.repository.TermsVersionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    @Mock
    private TermsTypeRepository termsTypeRepository;

    @Mock
    private TermsVersionRepository termsVersionRepository;

    @Mock
    private TermsClauseRepository termsClauseRepository;

    @Mock
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TermService termService;

    @Test
    void resolveAndValidateAgreementsRejectsDuplicateTermsVersionId() {
        List<TermReqDTO.AgreementItem> agreements = List.of(
                new TermReqDTO.AgreementItem(1L, true),
                new TermReqDTO.AgreementItem(1L, false)
        );

        assertThatThrownBy(() -> termService.resolveAndValidateAgreements(agreements))
                .isInstanceOfSatisfying(AgreementException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(AgreementErrorCode.DUPLICATE_TERMS_VERSION));

        verifyNoInteractions(termsVersionRepository);
    }
}
