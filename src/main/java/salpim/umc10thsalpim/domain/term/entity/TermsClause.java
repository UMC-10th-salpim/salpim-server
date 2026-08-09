package salpim.umc10thsalpim.domain.term.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 조항 (제N조)
 * content는 마크다운 문자열로 저장하며, 표(GFM table 문법) 등을 포함할 수 있다.
 * TEXT(@Lob)로 매핑한다. DB 컬럼 타입도 TEXT로 변경 필요.
 */
@Entity
@Table(name = "terms_clause",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terms_clause_version_clause_no",
                columnNames = {"terms_version_id", "clause_no"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_version_id", nullable = false)
    private TermsVersion termsVersion;

    // 조번호
    @Column(name = "clause_no", nullable = false)
    private Integer clauseNo;

    // 조항제목 (예: "목적", "용어의 정의")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 조항내용 (마크다운, 표 포함 가능)
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 노출순서
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    public TermsClause(TermsVersion termsVersion, Integer clauseNo, String title,
                        String content, Integer displayOrder) {
        this.termsVersion = termsVersion;
        this.clauseNo = clauseNo;
        this.title = title;
        this.content = content;
        this.displayOrder = displayOrder;
    }
}
