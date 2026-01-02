package com.goatwatches.service;

import com.goatwatches.entity.User;
import com.goatwatches.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        Optional<User> userOpt;
        if (loginIdentifier.contains("@")) {
            userOpt = userRepository.findByEmail(loginIdentifier);
        } else {
            userOpt = userRepository.findByUsername(loginIdentifier);
        }

        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginIdentifier));

        String password = user.getPassword();
        if (password == null) {
            password = ""; // Prevent null password for OAuth2 users to avoid 500 errors
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}