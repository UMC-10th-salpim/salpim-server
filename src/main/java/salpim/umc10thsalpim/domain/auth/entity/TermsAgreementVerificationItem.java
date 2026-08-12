package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.term.entity.TermsVersion;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "terms_agreement_verification_item")
public class TermsAgreementVerificationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_agreement_verification_id", nullable = false)
    private TermsAgreementVerification termsAgreementVerification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_version_id", nullable = false)
    private TermsVersion termsVersion;

    @Column(name = "agreed", nullable = false)
    private Boolean agreed;

    @Builder
    public TermsAgreementVerificationItem(
            TermsAgreementVerification termsAgreementVerification,
            TermsVersion termsVersion,
            Boolean agreed
    ) {
        this.termsAgreementVerification = termsAgreementVerification;
        this.termsVersion = termsVersion;
        this.agreed = agreed;
    }
}
