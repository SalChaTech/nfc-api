package com.salcatech.nfc_api.util;

import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import com.salcatech.nfc_api.exception.InvalidJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    public String generateTokenWithUserInfo(GoogleUserInfoDTO userInfo) {
        logger.info("🔵 JWT token oluşturuluyor - UserInfo: {}", userInfo);
        String token = Jwts.builder().setSubject((String) userInfo.getEmail())
                .claim("name", userInfo.getName())
                .claim("email", userInfo.getEmail())
                .claim("access_token", userInfo.getAccessToken())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
        logger.info("🔵 JWT token oluşturuldu - Uzunluk: {}", token.length());
        return token;
    }

    public GoogleUserInfoDTO extractUserInfo(String token) throws InvalidJwtException {
        Claims claims = parseClaims(token);

        GoogleUserInfoDTO userInfo = new GoogleUserInfoDTO();
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setName(claims.get("name", String.class));
        userInfo.setRole(claims.get("role", String.class));
        userInfo.setAccessToken(claims.get("access_token", String.class));

        return userInfo;
    }

    public String extractSubject(String token) throws InvalidJwtException {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) throws InvalidJwtException {
        Object role = parseClaims(token).get("role"); // tek string olarak saklanıyor
        return role != null ? role.toString() : "ROLE_USER"; // default ROLE_USER
    }

    public String extractName(String token) throws InvalidJwtException {
        return parseClaims(token).get("name", String.class);
    }

    public String extractEmail(String token) throws InvalidJwtException {
        return parseClaims(token).get("email", String.class);
    }

    public String extractAccessToken(String token) throws InvalidJwtException {
        return parseClaims(token).get("access_token", String.class);
    }


    public boolean validateToken(String token) throws InvalidJwtException {
        parseClaims(token);
        return true;

    }


    private Claims parseClaims(String token) throws InvalidJwtException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new InvalidJwtException("JWT parse edilemedi veya geçersiz: " + e.getMessage(), e);
        }
    }
}
