package com.goatwatches.controller;

import com.goatwatches.dto.AuthResponse;
import com.goatwatches.dto.ForgotPasswordRequest;
import com.goatwatches.dto.OAuthSignupRequest;
import com.goatwatches.dto.ResetPasswordRequest;
import com.goatwatches.dto.SignupRequest;
import com.goatwatches.exception.AuthException;
import com.goatwatches.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    @PostMapping("/user/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
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
    public ResponseEntity<?> adminSignup(@Valid @RequestBody SignupRequest request) {
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
    public ResponseEntity<?> completeOAuthSignup(@Valid @RequestBody OAuthSignupRequest request, Authentication authentication, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            return ResponseEntity.ok(authService.completeOAuthSignup(request, authentication, servletRequest, servletResponse));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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