package com.goatwatches.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SessionValidationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(SessionValidationFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String loginUserAgent = (String) session.getAttribute("USER_AGENT");
            String currentUserAgent = request.getHeader("User-Agent");

            if (loginUserAgent != null && !loginUserAgent.equals(currentUserAgent)) {
                logger.error("Session hijacking detected - invalidating session");
                session.invalidate();
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session hijacking detected: User-Agent mismatch");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}