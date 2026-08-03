package salpim.umc10thsalpim.domain.region.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import salpim.umc10thsalpim.domain.region.converter.RegionConverter;
import salpim.umc10thsalpim.domain.region.dto.RegionReqDTO;
import salpim.umc10thsalpim.domain.region.dto.RegionResDTO;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    @Transactional
    public RegionResDTO.ResolveResult resolve(RegionReqDTO.Resolve request) {
        String sido = normalizeRequired(request.sido());
        String sigungu = normalizeRequired(request.sigungu());
        String generalGu = normalizeOptional(request.generalGu());
        String administrativeArea = normalizeRequired(request.administrativeArea());

        List<String> regionNames = new ArrayList<>();
        Region parent = null;

        if (StringUtils.hasText(sido)) {
            parent = findOrCreateRegion(null, sido, RegionLevel.SIDO);
            regionNames.add(parent.getName());
        }

        if (StringUtils.hasText(sigungu)) {
            parent = findOrCreateRegion(parent, sigungu, RegionLevel.SIGUNGU);
            regionNames.add(parent.getName());
        }

        if (StringUtils.hasText(generalGu)) {
            parent = findOrCreateRegion(parent, generalGu, RegionLevel.GENERAL_GU);
            regionNames.add(parent.getName());
        }

        Region leafRegion = findOrCreateRegion(parent, administrativeArea, RegionLevel.ADMINISTRATIVE_AREA);
        regionNames.add(leafRegion.getName());

        return RegionConverter.toResolveResult(leafRegion, String.join(" ", regionNames));
    }

    private Region findOrCreateRegion(Region parent, String name, RegionLevel regionLevel) {
        if (parent == null) {
            return regionRepository.findByParentIsNullAndNameAndRegionLevel(name, regionLevel)
                    .orElseGet(() -> saveRegionWithRetry(null, name, regionLevel));
        }
        return regionRepository.findByParentAndNameAndRegionLevel(parent, name, regionLevel)
                .orElseGet(() -> saveRegionWithRetry(parent, name, regionLevel));
    }

    private Region saveRegionWithRetry(Region parent, String name, RegionLevel regionLevel) {
        try {
            return regionRepository.saveAndFlush(Region.create(parent, name, regionLevel));
        } catch (DataIntegrityViolationException e) {
            if (parent == null) {
                return regionRepository.findByParentIsNullAndNameAndRegionLevel(name, regionLevel)
                        .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_CONFLICT_RETRY_FAILED));
            }
            return regionRepository.findByParentAndNameAndRegionLevel(parent, name, regionLevel)
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_CONFLICT_RETRY_FAILED));
        }
    }

    private String normalizeRequired(String value) {
        String normalized = normalizeOptional(value);
        if (!StringUtils.hasText(normalized)) {
            throw new RegionException(RegionErrorCode.INVALID_REGION_REQUEST);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return normalized;
    }

    @Transactional(readOnly = true)
    public RegionResDTO.RegionListDTO getAncestorRegionList() {
        List<Region> regions = regionRepository.findAllByRegionLevelOrderById(RegionLevel.SIDO);

        return RegionConverter.toRegionResult(regions);
    }

    @Transactional(readOnly = true)
    public RegionResDTO.RegionListDTO getDescendantRegionList(Long ancestorRegionId) {

        regionRepository.findById(ancestorRegionId)
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        // TODO : 상위 지역이 아닌 하위 지역으로 요청 보냈을 때 예외처리

        List<Region> regions = regionRepository.findAllByParentIdOrderById(ancestorRegionId);

        return RegionConverter.toRegionResult(regions);
    }
}
