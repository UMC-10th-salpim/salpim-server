package salpim.umc10thsalpim.domain.term.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.term.enums.TermsVersionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 약관 버전
 * 하나의 약관종류(TermsType)는 시간에 따라 여러 버전을 가지며,
 * 게시(PUBLISHED)된 버전의 본문(TermsClause)은 절대 수정하지 않는다.
 * 변경이 필요하면 새로운 버전을 생성한다.
 */
@Entity
@Table(name = "terms_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_terms_version_type_version",
                        columnNames = {"terms_type_id", "version"}
                ),
                // status가 PUBLISHED일 때만 terms_type_id 값을 가지는 생성 컬럼(published_terms_type_id)에
                // 유니크 제약을 걸어, 약관종류당 PUBLISHED 버전이 동시에 2개 이상 존재할 수 없도록 DB 차원에서 강제한다.
                // MySQL은 partial unique index를 지원하지 않으므로 이 우회 방식을 사용한다.
                @UniqueConstraint(
                        name = "uk_terms_version_published_type",
                        columnNames = {"published_terms_type_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_type_id", nullable = false)
    private TermsType termsType;

    // 버전 (예: "1.0.0")
    @Column(name = "version", nullable = false, length = 20)
    private String version;

    // 상태 (DRAFT / PUBLISHED / ARCHIVED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TermsVersionStatus status;

    // 시행일
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    // 게시일시 (사전 공지 시각, 예약 게시 가능하므로 NULL 허용)
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // DB 생성 컬럼: status='PUBLISHED'일 때만 terms_type_id, 그 외에는 NULL.
    // uk_terms_version_published_type 유니크 제약이 이 컬럼에 걸려 있어
    // 동시 게시 요청이 와도 약관종류당 PUBLISHED 버전이 2개 이상 저장될 수 없다.
    @Getter(AccessLevel.NONE)
    @Column(name = "published_terms_type_id", insertable = false, updatable = false,
            columnDefinition = "BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'PUBLISHED' THEN terms_type_id END)")
    private Long publishedTermsTypeId;

    @OneToMany(mappedBy = "termsVersion")
    private List<TermsClause> clauses = new ArrayList<>();

    @Builder
    public TermsVersion(TermsType termsType, String version, LocalDate effectiveDate) {
        this.termsType = termsType;
        this.version = version;
        this.status = TermsVersionStatus.DRAFT;
        this.effectiveDate = effectiveDate;
        this.createdAt = LocalDateTime.now();
    }

    // 게시 처리, DRAFT 상태에서만 게시할 수 있다.
    public void publish() {
        if (this.status != TermsVersionStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태의 버전만 게시할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = TermsVersionStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    // 보관 처리. 새 버전이 게시될 때 기존 PUBLISHED 버전을 ARCHIVED로 전환하기 위함.
    public void archive() {
        if (this.status != TermsVersionStatus.PUBLISHED) {
            throw new IllegalStateException("PUBLISHED 상태의 버전만 보관 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = TermsVersionStatus.ARCHIVED;
    }

    public boolean isPublished() {
        return this.status == TermsVersionStatus.PUBLISHED;
    }
}