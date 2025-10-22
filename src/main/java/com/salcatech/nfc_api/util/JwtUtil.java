package com.salcatech.nfc_api.util;

import com.salcatech.nfc_api.dto.GoogleUserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private static String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private static String jwtExpirationMs;

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    public static String generateTokenWithUserInfo(GoogleUserInfo userInfo) {
        logger.info("🔵 JWT token oluşturuluyor - UserInfo: {}", userInfo);
        String token = Jwts.builder().setSubject((String) userInfo.getEmail())
                .claim("id", userInfo.getId())
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

    public static GoogleUserInfo extractUserInfo(String token) {
        Claims claims = parseClaims(token);

        GoogleUserInfo userInfo = new GoogleUserInfo();
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setName(claims.get("name", String.class));
        userInfo.setRole(claims.get("role", String.class));
        userInfo.setAccessToken(claims.get("access_token", String.class));
        userInfo.setId(claims.getId()); // subject = email ya da ID tercihine göre

        return userInfo;
    }

    public static String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public static String extractRole(String token) {
        Object role = parseClaims(token).get("role"); // tek string olarak saklanıyor
        return role != null ? role.toString() : "ROLE_USER"; // default ROLE_USER
    }

    public static String extractName(String token) {
        return parseClaims(token).get("name", String.class);
    }

    public static String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public static String extractAccessToken(String token) {
        return parseClaims(token).get("access_token", String.class);
    }


    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            logger.warn("🔴 Geçersiz JWT: {}", e.getMessage());
            return false;
        }
    }


    private static Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
