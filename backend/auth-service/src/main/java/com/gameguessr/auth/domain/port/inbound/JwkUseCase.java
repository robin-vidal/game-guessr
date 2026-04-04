package com.gameguessr.auth.domain.port.inbound;

import com.gameguessr.auth.application.rest.dto.JwkSetResponse;

public interface JwkUseCase {

    JwkSetResponse getJwkSet();
}
