package salpim.umc10thsalpim.domain.benefit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.benefit.enums.ApplicationType;
import salpim.umc10thsalpim.global.entity.BaseEntity;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "benefit_rule")
public class BenefitRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "welfare_benefit_id", nullable = false)
    private Long welfareBenefitId;

    @Column(name = "facility_type_id")
    private Long facilityTypeId;

    @Column(name = "application_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;
}
