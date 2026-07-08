package salpim.umc10thsalpim.domain.benefit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "target_description")
    private String targetDescription;

    @Column(name = "application_start_date")
    private LocalDate applicationStartDate;

    @Column(name = "application_end_date")
    private LocalDate applicationEndDate;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "application_method")
    private String applicationMethod;

    @Column(name = "application_url")
    private String applicationUrl;

    @Column(name = "contact")
    private String contact;

    @Column(name = "organization")
    private String organization;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
