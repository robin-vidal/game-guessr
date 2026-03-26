package com.gameguessr.auth.domain.port.inbound;

import com.gameguessr.auth.domain.model.User;

public interface AuthUseCase {

    User register(String username, String password);

    User login(String username, String password);

    void logout(String token);
}