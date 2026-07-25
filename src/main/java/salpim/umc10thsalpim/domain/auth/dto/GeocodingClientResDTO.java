package salpim.umc10thsalpim.domain.auth.dto;

public class GeocodingClientResDTO {

    public record Coordinate(
            Double latitude,
            Double longitude
    ) {
    }
}
