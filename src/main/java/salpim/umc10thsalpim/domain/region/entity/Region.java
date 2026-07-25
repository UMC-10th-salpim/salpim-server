package salpim.umc10thsalpim.domain.region.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "region",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_region_parent_name_level",
                        columnNames = {"parent_id", "name", "region_level"}
                )
        })
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_region_parent")
    )
    private Region parent;

    @Column(name = "parent_id", insertable = false, updatable = false)
    private Long parentId;

    @Builder.Default
    @OneToMany(mappedBy = "parent")
    private List<Region> children = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "region_level", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RegionLevel regionLevel;

    private Region(Region parent, String name, RegionLevel regionLevel) {
        this.parent = parent;
        this.name = name;
        this.regionLevel = regionLevel;
    }

    public static Region create(Region parent, String name, RegionLevel regionLevel) {
        return new Region(parent, name, regionLevel);
    }

    public Long getParentId() {
        return parent != null ? parent.getId() : parentId;
    }
}
