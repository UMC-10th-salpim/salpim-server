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
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.enums.TokenPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.AuthSecretHasher;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.util.Optional;

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
    private AuthSecretHasher authSecretHasher;

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
                memberRepository,
                authSecretHasher
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        when(tokenService.parseAccessToken("invalid-token"))
                .thenThrow(new AuthException(AuthErrorCode.INVALID_TOKEN));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE))
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authenticatesWhenCredentialFingerprintMatches() throws Exception {
        JwtAuthenticationFilter filter = createFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = Member.builder()
                .id(1L)
                .loginType(SocialProvider.LOCAL)
                .password("encoded-password")
                .build();
        TokenDTO.AccessTokenClaims claims = new TokenDTO.AccessTokenClaims(
                TokenPurpose.ACCESS,
                1L,
                "credential-fingerprint"
        );

        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenService.parseAccessToken("valid-token")).thenReturn(claims);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(authSecretHasher.createCredentialFingerprint(
                SocialProvider.LOCAL,
                "encoded-password"
        )).thenReturn("expected-fingerprint");
        when(authSecretHasher.matchesCredentialFingerprint(
                "credential-fingerprint",
                "expected-fingerprint"
        )).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(1L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void rejectsAuthenticationWhenCredentialFingerprintDoesNotMatch() throws Exception {
        JwtAuthenticationFilter filter = createFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Member member = Member.builder()
                .id(1L)
                .loginType(SocialProvider.LOCAL)
                .password("changed-password")
                .build();
        TokenDTO.AccessTokenClaims claims = new TokenDTO.AccessTokenClaims(
                TokenPurpose.ACCESS,
                1L,
                "previous-credential-fingerprint"
        );

        request.addHeader("Authorization", "Bearer invalidated-token");
        when(tokenService.parseAccessToken("invalidated-token")).thenReturn(claims);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(authSecretHasher.createCredentialFingerprint(
                SocialProvider.LOCAL,
                "changed-password"
        )).thenReturn("current-credential-fingerprint");
        when(authSecretHasher.matchesCredentialFingerprint(
                "previous-credential-fingerprint",
                "current-credential-fingerprint"
        )).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(request.getAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE))
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private JwtAuthenticationFilter createFilter() {
        return new JwtAuthenticationFilter(
                tokenService,
                memberRepository,
                authSecretHasher
        );
    }
}
