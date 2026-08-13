package salpim.umc10thsalpim.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import salpim.umc10thsalpim.domain.auth.security.JwtAuthenticationFilter;
import salpim.umc10thsalpim.domain.auth.security.JwtAuthenticationEntryPoint;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/login/local",
                                "/api/login/reissue",
                                "/api/login/kakao",
                                "/api/signup/phone/send",
                                "/api/signup/phone/verify",
                                "/api/signup/location/geocode",
                                "/api/signup/terms",
                                "/api/signup/local",
                                "/api/signup/kakao",
                                "/api/password-reset/verify"
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/password-reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/regions/resolve").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/regions",
                                "/api/regions/*/children"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/terms", "/api/terms/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/benefits/search").permitAll()
                        .requestMatchers(
                                new RegexRequestMatcher("^/api/benefits/\\d+$", HttpMethod.GET.name())
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/benefits/*/application-link").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/benefits/*/share").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recommendations/options/*").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://salpim.me",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
