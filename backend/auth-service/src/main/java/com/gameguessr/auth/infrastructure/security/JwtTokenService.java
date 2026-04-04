package com.gameguessr.auth.infrastructure.security;

import com.gameguessr.auth.domain.model.JwtTokenInfo;
import com.gameguessr.auth.domain.port.outbound.TokenService;
import com.gameguessr.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenService implements TokenService {

    private final JwtProperties jwtProperties;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtTokenService(JwtProperties jwtProperties) throws Exception {
        this.jwtProperties = jwtProperties;
        this.privateKey = parsePrivateKey(jwtProperties.getRsaPrivateKey());
        this.publicKey = parsePublicKey(jwtProperties.getRsaPublicKey());
    }

    private PrivateKey parsePrivateKey(String key) throws Exception {
        String normalizedKey = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalizedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey parsePublicKey(String key) throws Exception {
        String normalizedKey = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalizedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    @Override
    public String generateToken(UUID userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.get("userId", String.class));
    }

    @Override
    public JwtTokenInfo parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return JwtTokenInfo.builder()
                .userId(UUID.fromString(claims.get("userId", String.class)))
                .username(claims.getSubject())
                .build();
    }
}
