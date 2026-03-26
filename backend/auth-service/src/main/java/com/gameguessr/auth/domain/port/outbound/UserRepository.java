package com.gameguessr.auth.domain.port.outbound;

import com.gameguessr.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    User save(User user);

    boolean existsByUsername(String username);
}