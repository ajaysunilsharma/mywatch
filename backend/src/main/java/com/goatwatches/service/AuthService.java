package com.goatwatches.service;

import com.goatwatches.dto.AuthResponse;
import com.goatwatches.dto.ForgotPasswordRequest;
import com.goatwatches.dto.OAuthSignupRequest;
import com.goatwatches.dto.ResetPasswordRequest;
import com.goatwatches.dto.SignupRequest;
import com.goatwatches.entity.PasswordResetToken;
import com.goatwatches.entity.User;
import com.goatwatches.exception.AuthException;
import com.goatwatches.repository.PasswordResetTokenRepository;
import com.goatwatches.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final PasswordResetTokenRepository tokenRepository;

    @Value("${app.frontend.url:http://localhost:5000}")
    private List<String> frontendUrls;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("email", "Email already taken. Please Sign in or reset password.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthException("username", "Username not available");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user.setRole("USER");
        userRepository.save(user);
        return new AuthResponse("User registered successfully");
    }

    public AuthResponse adminSignup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("Email already taken");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthException("Username not available");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ADMIN");
        userRepository.save(user);
        return new AuthResponse("Admin registered successfully");
    }

    public AuthResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthException("Unauthorized");
        }
        
        // Check if user is in the middle of OAuth signup
        boolean isPreAuth = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRE_AUTH"));
        
        if (isPreAuth) {
            return new AuthResponse("", authentication.getName(), "ROLE_PRE_AUTH");
        }

        // Try finding by username first, then email (for OAuth users where name=email)
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(authentication.getName());
        }

        return userOpt.map(user -> {
            String role = user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole();
            return new AuthResponse(user.getUsername(), user.getEmail(), role);
        }).orElseThrow(() -> new AuthException("User not found"));
    }

    public AuthResponse completeOAuthSignup(OAuthSignupRequest request, Authentication authentication, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String username = request.getUsername();
        String email = authentication.getName(); // In PRE_AUTH state, name is the email

        if (userRepository.findByUsername(username).isPresent()) {
            throw new AuthException("Username not available");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(""); // No password for OAuth
        user.setRole("USER");
        userRepository.save(user);

        // Update Security Context to fully authenticated USER
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                username, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), servletRequest, servletResponse);

        // Bind the session to the User-Agent
        servletRequest.getSession().setAttribute("USER_AGENT", servletRequest.getHeader("User-Agent"));
        return new AuthResponse("Signup completed");
    }

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getEmail(); // Frontend sends 'email' key, but it could be username
        
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier, identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
            // invalidate existing token and create a new one
            existingToken.ifPresent(t -> {
                tokenRepository.delete(t);
                tokenRepository.flush();
            });
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, Instant.now().plus(24, ChronoUnit.HOURS));
            tokenRepository.save(resetToken);

            // Send email asynchronously to improve response time
            CompletableFuture.runAsync(() -> {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom("noreply@apexdial.com");
                    message.setTo(user.getEmail());
                    message.setSubject("Password Reset Request");
                    String baseUrl = (frontendUrls != null && !frontendUrls.isEmpty()) ? frontendUrls.get(0) : "http://localhost:5000";
                    message.setText("To reset your password, click the link below:\n" + baseUrl + "/reset-password?token=" + token);
                    mailSender.send(message);
                } catch (Exception e) {
                    // Log error but don't fail the request
                    logger.error("Failed to send password reset email", e);
                }
            });
            return new AuthResponse("Reset link has been sent to your registered email address");
        }else{
            throw new AuthException("User not found");
        }
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            tokenRepository.flush();
            throw new AuthException("Invalid or expired token");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);
        tokenRepository.delete(resetToken);
        tokenRepository.flush();
        
        return new AuthResponse("Password successfully reset");
    }

    public AuthResponse validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw new AuthException("Invalid or expired token");
        }
        
        User user = resetToken.getUser();
        return new AuthResponse(user.getUsername(), user.getEmail(), null);
    }
}