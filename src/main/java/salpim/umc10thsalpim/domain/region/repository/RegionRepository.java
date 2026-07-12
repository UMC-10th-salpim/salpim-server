package salpim.umc10thsalpim.domain.region.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByCityLAndCitySAndDong(String cityL, String cityS, String dong);
}
