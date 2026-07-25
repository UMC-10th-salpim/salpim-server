package salpim.umc10thsalpim.domain.map.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import salpim.umc10thsalpim.domain.member.entity.Member;

public interface FacilityRepository extends JpaRepository<Member, Long> {
    //임시 생성
}
