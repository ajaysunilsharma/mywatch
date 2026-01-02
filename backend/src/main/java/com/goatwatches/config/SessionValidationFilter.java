package com.goatwatches.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SessionValidationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String storedUserAgent = (String) session.getAttribute("USER_AGENT");
            String currentUserAgent = request.getHeader("User-Agent");

            if (storedUserAgent != null && !storedUserAgent.equals(currentUserAgent)) {
                session.invalidate();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session hijacking detected");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}