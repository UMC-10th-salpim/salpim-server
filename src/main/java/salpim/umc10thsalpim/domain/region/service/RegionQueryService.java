package salpim.umc10thsalpim.domain.region.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.*;

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

        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        Region sido = findAncestorRegionOrThrow(region, RegionLevel.SIDO);
        Region sigungu = findAncestorRegionOrThrow(region, RegionLevel.SIGUNGU);

        return new String[]{sido.getName(), sigungu.getName()};
    }

    public Region findAncestorRegion(Region region, RegionLevel targetLevel) {
        Region current = region;
        Set<Long> visitedIds = new HashSet<>();

        while (current != null) {
            if (!visitedIds.add(current.getId())) {
                throw new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID);
            }

            if (current.getRegionLevel() == targetLevel) {
                return current;
            }

            if (current.getParentId() == null) {
                return null;
            }

            current = regionRepository.findById(current.getParentId())
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        }

        return null;
    }

    public Region findAncestorRegionOrThrow(
            Region region,
            RegionLevel targetLevel
    ) {
        Region ancestorRegion = findAncestorRegion(region, targetLevel);

        if (ancestorRegion == null) {
            throw new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID);
        }

        return ancestorRegion;
    }

    public Map<Long, String> getAncestorRegionNameMap(Long regionId) {
        Map<Long, String> regionNameMap = new HashMap<>();
        if (regionId == null) {
            return regionNameMap;
        }

        List<Region> path = new ArrayList<>();
        Set<Long> visitedIds = new HashSet<>();
        Region current = regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        //엔티티 수집 (학익1동 -> 미추홀구 -> 인천광역시)
        while (current != null) {
            if (!visitedIds.add(current.getId())) {
                throw new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID);
            }
            path.add(current);

            if (current.getParentId() == null) break;

            current = regionRepository.findById(current.getParentId())
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        }
        //내려오면서 이름 누적 조립
        Collections.reverse(path);
        StringBuilder fullName = new StringBuilder();

        for (Region r : path) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(r.getName());

            // Map에 저장 (예: 1L -> "인천광역시", 2L -> "인천광역시 미추홀구")
            regionNameMap.put(r.getId(), fullName.toString());
        }

        return regionNameMap;
    }
}
