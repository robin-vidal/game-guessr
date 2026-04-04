package com.gameguessr.auth.application.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwkSetResponse {

    private List<Jwk> keys;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Jwk {
        private String kty;
        private String use;
        private String kid;
        private String alg;
        private String n;
        private String e;
    }
}
