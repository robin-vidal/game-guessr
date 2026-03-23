package com.gameguessr.auth.application.service;

import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.gameguessr.auth.domain.port.outbound.TokenService tokenService;

    public String generateTokenForUser(User user) {
        return tokenService.generateToken(user.getUsername());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User createUser(String username, String encodedPassword) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .build();
    }
}