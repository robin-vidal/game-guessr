package com.gameguessr.auth.domain.port.inbound;

import com.gameguessr.auth.domain.model.JwtTokenInfo;
import com.gameguessr.auth.domain.model.LoginResult;
import com.gameguessr.auth.domain.model.User;

public interface AuthUseCase {

    User register(String username, String password);

    LoginResult login(String username, String password);

    void logout(String token);

    JwtTokenInfo getMe(String token);
}