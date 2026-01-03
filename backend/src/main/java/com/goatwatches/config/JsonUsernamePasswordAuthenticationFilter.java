package com.goatwatches.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goatwatches.dto.AuthRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;


    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper, AuthenticationManager authenticationManager) {
        this.objectMapper = objectMapper;
        this.setAuthenticationManager(authenticationManager);
        this.setFilterProcessesUrl("/api/auth/login");
        this.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        this.setAuthenticationSuccessHandler((request, response, authentication) -> {
            // Bind session to User-Agent to prevent hijacking
            request.getSession().setAttribute("USER_AGENT", request.getHeader("User-Agent"));

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(java.util.Map.of("message", "Login successful")));
        });

        this.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(java.util.Map.of("error", "Invalid credentials")));
        });
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        try {
            AuthRequest authRequest = objectMapper.readValue(request.getInputStream(), AuthRequest.class);

            UsernamePasswordAuthenticationToken authRequestToken = new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), authRequest.getPassword());

            setDetails(request, authRequestToken);

            return this.getAuthenticationManager().authenticate(authRequestToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }
}