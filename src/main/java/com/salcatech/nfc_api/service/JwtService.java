package com.salcatech.nfc_api.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 saat
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenWithUserInfo(Map<String, Object> userInfo) {
        logger.info("🔵 JWT token oluşturuluyor - UserInfo: {}", userInfo);
        
        String token = Jwts.builder()
                .setSubject((String) userInfo.get("email"))
                .claim("name", userInfo.get("name"))
                .claim("picture", userInfo.get("picture"))
                .claim("email", userInfo.get("email"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000)) // 1 saat
                .signWith(getSigningKey())
                .compact();
                
        logger.info("🔵 JWT token oluşturuldu - Uzunluk: {}", token.length());
        return token;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
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

    public boolean validateToken(String token) {
        try {
            logger.info("🔵 JWT token doğrulanıyor...");
            parseClaims(token);
            logger.info("🔵 JWT token geçerli!");
            return true;
        } catch (JwtException e) {
            logger.warn("🔴 JWT token geçersiz: {}", e.getMessage());
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
