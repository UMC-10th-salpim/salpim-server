package salpim.umc10thsalpim.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "password_reset_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_password_reset_token_member",
                        columnNames = "member_id"
                ),
                @UniqueConstraint(
                        name = "uk_password_reset_token_id_hash",
                        columnNames = "token_id_hash"
                )
        }
)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(
                    name = "fk_password_reset_token_member"
            )
    )
    private Member member;

    @Column(name = "token_id_hash", nullable = false, length = 100)
    private String tokenIdHash;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public void replaceToken(String tokenIdHash, LocalDateTime expiredAt) {
        this.tokenIdHash = tokenIdHash;
        this.expiredAt = expiredAt;
        this.usedAt = null;
    }

    public boolean isUsableAt(LocalDateTime now) {
        return usedAt == null && expiredAt.isAfter(now);
    }

    public void consume(LocalDateTime now) {
        this.usedAt = now;
    }

}
