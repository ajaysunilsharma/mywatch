package com.goatwatches.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.goatwatches.service.CustomOidcUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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

    private final ObjectMapper objectMapper;

    public SecurityConfig(CustomOidcUserService customOidcUserService, ObjectMapper objectMapper) {
        this.customOidcUserService = customOidcUserService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // Set the name of the attribute the CsrfToken will be populated on
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )
            .addFilterAfter(new SessionValidationFilter(), SecurityContextHolderFilter.class)
            .addFilterBefore(new JsonUsernamePasswordAuthenticationFilter(objectMapper,authenticationManager), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/user/signup", "/api/auth/admin/signup", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/reset-password/validate", "/api/auth/oauth/complete-signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/watches/{id}/reviews").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/watches/{id}/vote").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/watches").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/watches/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/watches/{id}").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/watches/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler((request, response, authentication) -> {
                    // Bind session to User-Agent to prevent hijacking
                    request.getSession().setAttribute("USER_AGENT", request.getHeader("User-Agent"));

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
            ).logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write(objectMapper.writeValueAsString(java.util.Map.of("message", "Logged out successfully")));
                        }).deleteCookies("SESSION", "JSESSIONID"));
            
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