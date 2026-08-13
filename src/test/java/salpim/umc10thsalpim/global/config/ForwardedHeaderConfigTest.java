package salpim.umc10thsalpim.global.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.assertj.core.api.Assertions.assertThat;

class ForwardedHeaderConfigTest {

    @Test
    void removesForwardedHeadersWithoutChangingResolvedRemoteAddress() throws Exception {
        ForwardedHeaderFilter filter = new ForwardedHeaderConfig()
                .forwardedHeaderRemovalFilter()
                .getFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.20");
        request.addHeader("Forwarded", "for=203.0.113.10;proto=http");
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        request.addHeader("X-Forwarded-Proto", "http");

        filter.doFilter(request, new MockHttpServletResponse(), (filteredRequest, response) -> {
            HttpServletRequest httpRequest = (HttpServletRequest) filteredRequest;
            assertThat(httpRequest.getRemoteAddr()).isEqualTo("198.51.100.20");
            assertThat(httpRequest.getHeader("Forwarded")).isNull();
            assertThat(httpRequest.getHeader("X-Forwarded-For")).isNull();
            assertThat(httpRequest.getHeader("X-Forwarded-Proto")).isNull();
        });
    }
}
