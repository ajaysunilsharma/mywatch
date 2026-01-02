package com.goatwatches.config;


import com.goatwatches.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend.url:http://localhost:5000}")
    private List<String> frontendUrls;


    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/user/signup", "/api/auth/admin/signup", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/reset-password/validate", "/api/auth/oauth/complete-signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/watches/{id}/reviews").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/watches/{id}/vote").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/watches").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/watches/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/watches/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/watches/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler((request, response, authentication) -> {
                    boolean isPreAuth = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PRE_AUTH"));
                    
                    String targetUrl = isPreAuth ? "/oauth/set-username" : "/";
                    String baseUrl = (frontendUrls != null && !frontendUrls.isEmpty()) ? frontendUrls.get(0) : "http://localhost:5000";
                    System.out.println("the redirect url is " + baseUrl + targetUrl);
                    response.sendRedirect(baseUrl + targetUrl);
                })
                .failureHandler((request, response, exception) -> {
                    System.out.println("------------------------------------------------");
                    System.out.println("OAuth2 Login Failed: " + exception.getMessage());
                    exception.printStackTrace();
                    System.out.println("------------------------------------------------");
                    String baseUrl = (frontendUrls != null && !frontendUrls.isEmpty()) ? frontendUrls.get(0) : "http://localhost:5000";
                    response.sendRedirect(baseUrl + "/login?error");
                })
            );
            
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String baseUrl = (frontendUrls != null && !frontendUrls.isEmpty()) ? frontendUrls.get(0) : "http://localhost:5000";
        configuration.setAllowedOrigins(List.of(baseUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}