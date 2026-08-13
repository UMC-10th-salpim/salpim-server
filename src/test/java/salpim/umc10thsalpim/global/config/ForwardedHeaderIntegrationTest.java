package salpim.umc10thsalpim.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.service.LocalLoginService;
import salpim.umc10thsalpim.domain.member.enums.WordSize;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ForwardedHeaderIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private LocalLoginService localLoginService;

    @Autowired
    private ForwardedHeaderConfig forwardedHeaderConfig;

    @Test
    void usesXForwardedForFromInternalProxyAndIgnoresStandardForwardedHeader() throws Exception {
        when(localLoginService.login(any(), anyString())).thenReturn(
                new AuthResDTO.TokenResult("access-token", "refresh-token", WordSize.MEDIUM)
        );

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(
                                "http://127.0.0.1:" + port + "/api/login/local"
                        ))
                        .header("Content-Type", "application/json")
                        .header("X-Forwarded-For", "198.51.100.20")
                        .header("X-Forwarded-Proto", "https")
                        .header("Forwarded", "for=203.0.113.10;proto=http")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"phoneNumber\":\"01012345678\",\"password\":\"123456\"}"
                        ))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Strict-Transport-Security")).isPresent();
        org.mockito.Mockito.verify(localLoginService).login(
                any(),
                org.mockito.ArgumentMatchers.eq("198.51.100.20")
        );
        assertThat(forwardedHeaderConfig).isNotNull();
    }
}
