package salpim.umc10thsalpim.domain.benefit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.benefit.enums.AgeConditionStatus;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "welfare_benefit")
public class WelfareBenefit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "easy_summary", nullable = false)
    private String easySummary;

    @Column(name = "who_can_receive", nullable = false)
    private String whoCanReceive;

    @Column(name = "what_you_receive", nullable = false)
    private String whatYouReceive;

    @Column(name = "recommended_for", nullable = false)
    private String recommendedFor;

    @Column(name = "application_start_date")
    private LocalDate applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDate applicationEndDate;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_scope", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegionScope regionScope;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "application_url")
    private String applicationUrl;

    @Column(name = "contact")
    private String contact;

    @Column(name = "organization")
    private String organization;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "age_condition_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AgeConditionStatus ageConditionStatus = AgeConditionStatus.UNKNOWN;
}
