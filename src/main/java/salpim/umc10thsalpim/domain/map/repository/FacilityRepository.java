package salpim.umc10thsalpim.domain.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Member;

public interface FacilityRepository extends JpaRepository<Member, Long> {
}
