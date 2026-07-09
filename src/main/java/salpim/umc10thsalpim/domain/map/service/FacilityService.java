package salpim.umc10thsalpim.domain.map.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import salpim.umc10thsalpim.domain.map.dto.MapResponseDto;
import salpim.umc10thsalpim.domain.map.repository.FacilityRepository;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public MapResponseDto.FacilityInfoResponse getFacilityInfo(
            Long memberId, String facilityName, String address, String phone, String distance
    ) {
        return null;//임시
    }
}
