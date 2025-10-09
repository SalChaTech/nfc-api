package com.salcatech.nfc_api.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 🔹 username + roles bilgisiyle token üret
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 saat
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenByProductIdAndUserId(String productId, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600 * 1000);
        return Jwts.builder().setSubject(userId != null ? userId.toString() : "").setIssuedAt(now).setExpiration(expiry).addClaims(Map.of("productId", productId)).signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes()).compact();
    }

    public String generateTokenWithUserInfo(Map<String, Object> userInfo) {
        logger.info("🔵 JWT token oluşturuluyor - UserInfo: {}", userInfo);
        String token = Jwts.builder().setSubject((String) userInfo.get("email")).claim("name", userInfo.get("name")).claim("picture", userInfo.get("picture")).claim("email", userInfo.get("email")).claim("access_token", userInfo.get("access_token")).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)).signWith(getSigningKey()).compact();
        logger.info("🔵 JWT token oluşturuldu - Uzunluk: {}", token.length());
        return token;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUserIdFromToken(String token) {
        try {
            token = token.trim();
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.warn("🔴 Token'dan userId çıkarılamadı: {}", e.getMessage());
            return null;
        }
    }

    public String extractProductIdFromToken(String token) {
        token = token.trim();
        Claims claims = Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token).getBody();
        return claims.get("productId", String.class);
    }

    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }


    public String extractName(String token) {
        return parseClaims(token).get("name", String.class);
    }

    public String extractPicture(String token) {
        return parseClaims(token).get("picture", String.class);
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractAccessToken(String token) {
        return parseClaims(token).get("access_token", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            logger.warn("🔴 Geçersiz JWT: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
