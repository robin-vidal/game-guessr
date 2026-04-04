package com.gameguessr.auth.application.service;

import com.gameguessr.auth.application.rest.dto.JwkSetResponse;
import com.gameguessr.auth.domain.port.inbound.JwkUseCase;
import com.gameguessr.auth.infrastructure.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class JwkApplicationService implements JwkUseCase {

    private final JwtProperties jwtProperties;

    @Override
    public JwkSetResponse getJwkSet() {
        try {
            PublicKey publicKey = parsePublicKey(jwtProperties.getRsaPublicKey());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);

            byte[] modulusBytes = rsaSpec.getModulus().toByteArray();
            byte[] exponentBytes = rsaSpec.getPublicExponent().toByteArray();

            String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(stripLeadingZeros(modulusBytes));
            String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(stripLeadingZeros(exponentBytes));

            String kid = computeKid(stripLeadingZeros(modulusBytes));

            JwkSetResponse.Jwk jwk = JwkSetResponse.Jwk.builder()
                    .kty("RSA")
                    .use("sig")
                    .kid(kid)
                    .alg("RS256")
                    .n(modulus)
                    .e(exponent)
                    .build();

            return JwkSetResponse.builder()
                    .keys(java.util.List.of(jwk))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWK Set", e);
        }
    }

    private PublicKey parsePublicKey(String key) throws Exception {
        String normalizedKey = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalizedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(decoded));
    }

    private byte[] stripLeadingZeros(byte[] array) {
        int i = 0;
        while (i < array.length - 1 && array[i] == 0) {
            i++;
        }
        if (i == 0) {
            return array;
        }
        byte[] result = new byte[array.length - i];
        System.arraycopy(array, i, result, 0, result.length);
        return result;
    }

    private String computeKid(byte[] modulusBytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(modulusBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
    }
}
