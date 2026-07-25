package salpim.umc10thsalpim.domain.region.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByParentIsNullAndNameAndRegionLevel(String name, RegionLevel regionLevel);

    Optional<Region> findByParentAndNameAndRegionLevel(Region parent, String name, RegionLevel regionLevel);
}
