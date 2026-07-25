package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.client.GeocodingClient;
import salpim.umc10thsalpim.domain.auth.converter.AuthConverter;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.dto.GeocodingClientResDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeocodingService {

    private final GeocodingClient geocodingClient;

    public AuthResDTO.GeocodeResult geocode(String roadAddress) {
        String trimmedRoadAddress = roadAddress.trim();
        GeocodingClientResDTO.Coordinate coordinate = geocodingClient.searchCoordinate(trimmedRoadAddress);
        return AuthConverter.toGeocodeResult(trimmedRoadAddress, coordinate);
    }
}
