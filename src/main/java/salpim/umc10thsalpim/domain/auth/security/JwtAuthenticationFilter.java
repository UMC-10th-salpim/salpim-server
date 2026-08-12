package salpim.umc10thsalpim.domain.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import salpim.umc10thsalpim.domain.auth.dto.TokenDTO;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.AuthSecretHasher;
import salpim.umc10thsalpim.domain.auth.service.TokenService;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final AuthSecretHasher authSecretHasher;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveBearerToken(request);
        if (token != null) {
            authenticate(request, token);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            TokenDTO.AccessTokenClaims claims = tokenService.parseAccessToken(token);

            memberRepository.findById(claims.memberId())
                    .filter(member -> authSecretHasher.matchesCredentialFingerprint(
                            claims.credentialFingerprint(),
                            authSecretHasher.createCredentialFingerprint(
                                    member.getLoginType(),
                                    member.getPassword()
                            )
                    ))
                    .ifPresentOrElse(
                            member -> {
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                member.getId(),
                                                null,
                                                List.of()
                                        );
                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                            },
                            () -> request.setAttribute(
                                    JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE,
                                    AuthErrorCode.INVALID_TOKEN
                            )
                    );
        } catch (AuthException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute(
                    JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE,
                    e.getErrorCode()
            );
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
