package salpim.umc10thsalpim.domain.benefit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareCategory;

import java.util.List;

public interface WelfareCategoryRepository extends JpaRepository<WelfareCategory, Long> {

    List<WelfareCategory> findAllById(Long id);
}
