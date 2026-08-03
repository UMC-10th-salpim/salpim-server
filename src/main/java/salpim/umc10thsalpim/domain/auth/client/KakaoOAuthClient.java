package salpim.umc10thsalpim.domain.auth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import salpim.umc10thsalpim.domain.auth.config.KakaoProperties;
import salpim.umc10thsalpim.domain.auth.dto.KakaoOAuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient.Builder restClientBuilder;
    private final KakaoProperties kakaoProperties;
    private final ObjectMapper objectMapper;

    public KakaoOAuthResDTO.Token requestToken(String authorizationCode) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);
        form.add("client_id", kakaoProperties.getClientId());
        form.add("redirect_uri", kakaoProperties.getRedirectUri());
        form.add("code", authorizationCode);

        if (StringUtils.hasText(kakaoProperties.getClientSecret())) {
            form.add("client_secret", kakaoProperties.getClientSecret());
        }

        try {
            KakaoOAuthResDTO.Token response = restClientBuilder.build()
                    .post()
                    .uri(kakaoProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoOAuthResDTO.Token.class);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new AuthException(AuthErrorCode.KAKAO_API_ERROR);
            }

            return response;
        } catch (AuthException e) {
            throw e;
        } catch (RestClientResponseException e) {
            if (isInvalidGrant(e)) {
                throw new AuthException(AuthErrorCode.KAKAO_AUTHORIZATION_CODE_INVALID);
            }
            throw new AuthException(AuthErrorCode.KAKAO_API_ERROR);
        } catch (RestClientException e) {
            throw new AuthException(AuthErrorCode.KAKAO_API_ERROR);
        }
    }

    public KakaoOAuthResDTO.UserInfo requestUserInfo(String kakaoAccessToken) {
        try {
            KakaoOAuthResDTO.UserInfo response = restClientBuilder.build()
                    .get()
                    .uri(kakaoProperties.getUserInfoUri())
                    .header("Authorization", BEARER_PREFIX + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoOAuthResDTO.UserInfo.class);

            if (response == null) {
                throw new AuthException(AuthErrorCode.KAKAO_API_ERROR);
            }

            return response;
        } catch (AuthException e) {
            throw e;
        } catch (RestClientException e) {
            throw new AuthException(AuthErrorCode.KAKAO_API_ERROR);
        }
    }

    private boolean isInvalidGrant(RestClientResponseException exception) {
        try {
            return "invalid_grant".equals(
                    objectMapper.readTree(exception.getResponseBodyAsByteArray())
                            .path("error")
                            .asText()
            );
        } catch (IOException e) {
            return false;
        }
    }
}
