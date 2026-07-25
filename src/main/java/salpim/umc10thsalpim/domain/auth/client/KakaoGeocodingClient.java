package salpim.umc10thsalpim.domain.auth.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import salpim.umc10thsalpim.domain.auth.config.GeocodingProperties;
import salpim.umc10thsalpim.domain.auth.dto.KakaoGeocodingResDTO;
import salpim.umc10thsalpim.domain.auth.exception.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.dto.GeocodingClientResDTO;

@Component
@RequiredArgsConstructor
public class KakaoGeocodingClient implements GeocodingClient {

    private static final String KAKAO_AUTH_PREFIX = "KakaoAK ";

    private final RestClient.Builder restClientBuilder;
    private final GeocodingProperties geocodingProperties;

    @Override
    public GeocodingClientResDTO.Coordinate searchCoordinate(String roadAddress) {
        if (!StringUtils.hasText(geocodingProperties.getApiKey())) {
            throw new AuthException(AuthErrorCode.GEOCODING_API_ERROR);
        }

        try {
            KakaoGeocodingResDTO.SearchAddress response = restClientBuilder
                    .baseUrl(geocodingProperties.getBaseUrl())
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", roadAddress)
                            .build())
                    .header("Authorization", KAKAO_AUTH_PREFIX + geocodingProperties.getApiKey())
                    .retrieve()
                    .body(KakaoGeocodingResDTO.SearchAddress.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                throw new AuthException(AuthErrorCode.GEOCODING_RESULT_NOT_FOUND);
            }

            KakaoGeocodingResDTO.AddressDocument document = response.documents().get(0);
            return new GeocodingClientResDTO.Coordinate(
                    Double.parseDouble(document.y()),
                    Double.parseDouble(document.x())
            );
        } catch (AuthException e) {
            throw e;
        } catch (RestClientException | NumberFormatException e) {
            throw new AuthException(AuthErrorCode.GEOCODING_API_ERROR);
        }
    }
}
