package salpim.umc10thsalpim.domain.term.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.member.entity.Member;

import java.time.LocalDateTime;

/**
 * 회원 약관 동의 이력
 * 한 번 생성된 동의 이력은 수정하지 않는다 (재동의 시 새로운 row를 추가).
 */
@Entity
@Table(name = "member_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_version_id", nullable = false)
    private TermsVersion termsVersion;

    // 동의여부
    @Column(name = "agreed", nullable = false)
    private Boolean agreed;

    // 동의일시
    @Column(name = "agreed_at", nullable = false, updatable = false)
    private LocalDateTime agreedAt;

    @Builder
    public MemberAgreement(Member member, TermsVersion termsVersion, Boolean agreed) {
        this.member = member;
        this.termsVersion = termsVersion;
        this.agreed = agreed;
        this.agreedAt = LocalDateTime.now();
    }
}
