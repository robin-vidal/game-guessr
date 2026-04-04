package com.gameguessr.auth.application.service;

import com.gameguessr.auth.domain.model.JwtTokenInfo;
import com.gameguessr.auth.domain.model.LoginResult;
import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.inbound.AuthUseCase;
import com.gameguessr.auth.domain.port.outbound.TokenBlacklist;
import com.gameguessr.auth.domain.port.outbound.TokenService;
import com.gameguessr.auth.domain.port.outbound.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final TokenBlacklist tokenBlacklist;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .build();
        return userRepository.save(user);
    }

    @Override
    public LoginResult login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = tokenService.generateToken(user.getId(), user.getUsername());

        return LoginResult.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.invalidate(token);
    }

    @Override
    public JwtTokenInfo getMe(String token) {
        return tokenService.parseToken(token);
    }
}