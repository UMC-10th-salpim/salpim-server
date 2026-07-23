package salpim.umc10thsalpim.domain.region.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
