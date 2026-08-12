package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "terms_agreement_verification",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terms_agreement_verification_phone",
                columnNames = "phone_number"
        )
)
public class TermsAgreementVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "agreement_hash", nullable = false, length = 255)
    private String agreementHash;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "termsAgreementVerification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TermsAgreementVerificationItem> items = new ArrayList<>();

    @Builder
    public TermsAgreementVerification(String phoneNumber, String agreementHash, LocalDateTime expiredAt) {
        this.phoneNumber = phoneNumber;
        this.agreementHash = agreementHash;
        this.expiredAt = expiredAt;
    }

    public void updateAgreement(String agreementHash, LocalDateTime expiredAt) {
        this.agreementHash = agreementHash;
        this.expiredAt = expiredAt;
        this.items.clear();
    }

    public void addItem(TermsAgreementVerificationItem item) {
        this.items.add(item);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiredAt.isAfter(now);
    }
}
