package salpim.umc10thsalpim.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class KakaoGeocodingResDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchAddress(
            List<AddressDocument> documents
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressDocument(
            String x,
            String y
    ) {
    }
}
