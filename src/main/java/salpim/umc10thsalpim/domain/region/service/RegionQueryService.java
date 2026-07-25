package salpim.umc10thsalpim.domain.region.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionQueryService {

    private final RegionRepository regionRepository;

    // Member의 regionId로부터 상위 (시/도, 시/군/구) 이름을 탐색하여 반환
    public String[] getSidoAndSigungu(Long regionId) {
        if (regionId == null) {
            return new String[]{null, null};
        }

        Region current = regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        String sido = null;
        String sigungu = null;
        Set<Long> visitedIds = new HashSet<>();

        while (current != null) {
            if (!visitedIds.add(current.getId())) {
                throw new RegionException(RegionErrorCode.INVALID_REGION_REQUEST);
            }

            if (current.getRegionLevel() == RegionLevel.SIDO) {
                sido = current.getName();
            } else if (current.getRegionLevel() == RegionLevel.SIGUNGU) {
                sigungu = current.getName();
            }

            if (current.getParentId() == null) {
                break;
            }

            Long parentId = current.getParentId();
            current = regionRepository.findById(parentId)
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        }

        return new String[]{sido, sigungu};
    }
}
