package com.goatwatches.controller;

import com.goatwatches.dto.AuthRequest;
import com.goatwatches.dto.AuthResponse;
import com.goatwatches.exception.AuthException;
import com.goatwatches.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return ResponseEntity.ok(authService.login(request, servletRequest, servletResponse));
    }

    @PostMapping("/user/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
        } catch (AuthException e) {
            if (e.getField() != null) {
                return ResponseEntity.badRequest().body(Map.of("field", e.getField(), "error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/signup")
    public ResponseEntity<?> adminSignup(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.adminSignup(request));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            return ResponseEntity.ok(authService.getCurrentUser(authentication));
        } catch (AuthException e) {
            if ("Unauthorized".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/oauth/complete-signup")
    public ResponseEntity<?> completeOAuthSignup(@RequestBody Map<String, String> request, Authentication authentication, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            return ResponseEntity.ok(authService.completeOAuthSignup(request, authentication, servletRequest, servletResponse));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        return ResponseEntity.ok(authService.logout(request, response, authentication));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(authService.resetPassword(request));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            return ResponseEntity.ok(authService.validateResetToken(token));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}