package salpim.umc10thsalpim.domain.auth.client;

import salpim.umc10thsalpim.domain.auth.dto.GeocodingClientResDTO;

public interface GeocodingClient {

    GeocodingClientResDTO.Coordinate searchCoordinate(String roadAddress);
}
