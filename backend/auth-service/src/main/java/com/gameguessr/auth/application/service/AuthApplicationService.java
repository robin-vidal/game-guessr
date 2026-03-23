package com.gameguessr.auth.application.service;

import com.gameguessr.auth.application.service.TokenService;
import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.inbound.AuthUseCase;
import com.gameguessr.auth.domain.port.outbound.TokenBlacklist;
import com.gameguessr.auth.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final TokenBlacklist tokenBlacklist;

    @Override
    @Transactional
    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        String encodedPassword = tokenService.encodePassword(password);
        User user = tokenService.createUser(username, encodedPassword);
        return userRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!tokenService.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return tokenService.generateTokenForUser(user);
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.invalidate(token);
    }
}