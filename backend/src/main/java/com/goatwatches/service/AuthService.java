package com.goatwatches.service;

import com.goatwatches.dto.AuthRequest;
import com.goatwatches.dto.AuthResponse;
import com.goatwatches.entity.User;
import com.goatwatches.exception.AuthException;
import com.goatwatches.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    
    // In-memory token store. In production, store this in the database with an expiry.
    private final Map<String, String> passwordResetTokens = new ConcurrentHashMap<>();

    @Value("${app.frontend.url:http://localhost:5000}")
    private List<String> frontendUrls;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public AuthResponse login(AuthRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String loginIdentifier = request.getUsername(); // Can be username or email
        String resolvedUsername = loginIdentifier;

        // If input looks like an email, try to find the username
        if (loginIdentifier.contains("@")) {
            resolvedUsername = userRepository.findByEmail(loginIdentifier)
                    .map(User::getUsername)
                    .orElse(loginIdentifier); // Fallback to let auth manager fail naturally
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(resolvedUsername, request.getPassword())
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        // Bind the session to the User-Agent to prevent hijacking
        servletRequest.getSession().setAttribute("USER_AGENT", servletRequest.getHeader("User-Agent"));
        
        return new AuthResponse("Login successful");
    }

    public AuthResponse signup(Map<String, String> request) {
        if (userRepository.findByEmail(request.get("email")).isPresent()) {
            throw new AuthException("email", "Email already taken. Please Sign in or reset password.");
        }
        if (userRepository.findByUsername(request.get("username")).isPresent()) {
            throw new AuthException("username", "Username not available");
        }

        User user = new User();
        user.setUsername(request.get("username"));
        user.setEmail(request.get("email"));
        user.setPassword(passwordEncoder.encode(request.get("password")));
        
        user.setRole("USER");
        userRepository.save(user);
        return new AuthResponse("User registered successfully");
    }

    public AuthResponse adminSignup(Map<String, String> request) {
        if (userRepository.findByEmail(request.get("email")).isPresent()) {
            throw new AuthException("Email already taken");
        }
        if (userRepository.findByUsername(request.get("username")).isPresent()) {
            throw new AuthException("Username not available");
        }
        User user = new User();
        user.setUsername(request.get("username"));
        user.setEmail(request.get("email"));
        user.setPassword(passwordEncoder.encode(request.get("password")));
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

    public AuthResponse completeOAuthSignup(Map<String, String> request, Authentication authentication, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String username = request.get("username");
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
        
        return new AuthResponse("Signup completed");
    }

    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return new AuthResponse("Logged out");
    }

    public AuthResponse forgotPassword(Map<String, String> request) {
        String identifier = request.get("email"); // Frontend sends 'email' key, but it could be username
        
        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            passwordResetTokens.put(token, user.getEmail()); // Store actual email mapped to token
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@apexdial.com");
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            String baseUrl = (frontendUrls != null && !frontendUrls.isEmpty()) ? frontendUrls.get(0) : "http://localhost:5000";
            message.setText("To reset your password, click the link below:\n" + baseUrl + "/reset-password?token=" + token);
            mailSender.send(message);
        }
        return new AuthResponse("reset link has been sent to your registered email address");
    }

    public AuthResponse resetPassword(Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        String email = passwordResetTokens.remove(token);
        if (email == null) {
            throw new AuthException("Invalid or expired token");
        }
        
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return new AuthResponse("Password successfully reset");
    }

    public AuthResponse validateResetToken(String token) {
        String email = passwordResetTokens.get(token);
        if (email == null) {
            throw new AuthException("Invalid or expired token");
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse(user.getUsername(), user.getEmail(), null);
    }
}