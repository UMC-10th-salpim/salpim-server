package salpim.umc10thsalpim.domain.term.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.term.enums.TermsTypeCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 약관 종류
 * 예: 서비스 이용약관, 개인정보 수집 및 이용 동의, 민감정보 수집 및 이용 동의, 위치정보 수집 및 이용 동의
 */
@Entity
@Table(name = "terms_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 약관코드 (SERVICE, PRIVACY, SENSITIVE_INFO, LOCATION)
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 30)
    private TermsTypeCode code;

    // 약관명 (예: "서비스 이용약관")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 필수여부
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    // 노출순서
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @OneToMany(mappedBy = "termsType")
    private List<TermsVersion> versions = new ArrayList<>();

    @Builder
    public TermsType(TermsTypeCode code, String name, Boolean isRequired, Integer displayOrder) {
        this.code = code;
        this.name = name;
        this.isRequired = isRequired;
        this.displayOrder = displayOrder;
    }
}