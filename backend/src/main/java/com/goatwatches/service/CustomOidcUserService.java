package com.goatwatches.service;

import com.goatwatches.entity.User;
import com.goatwatches.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");

        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return new DefaultOidcUser(
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo(),
                        "email"
                );
            } else {
                // New user: Return with PRE_AUTH role so we can redirect to username selection
                return new DefaultOidcUser(
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRE_AUTH")),
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo(),
                        "email"
                );
            }
        }
        return oidcUser;
    }
}
