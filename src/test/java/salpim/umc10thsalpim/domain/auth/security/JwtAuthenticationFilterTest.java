package salpim.umc10thsalpim.domain.auth.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forwardsTokenFailureToAuthenticationEntryPoint() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                tokenService,
                memberRepository
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        when(tokenService.validateAccessTokenAndGetMemberId("invalid-token"))
                .thenThrow(new AuthException(AuthErrorCode.INVALID_TOKEN));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE))
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
