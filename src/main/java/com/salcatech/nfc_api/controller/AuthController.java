package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.service.GoogleAuthService;
import com.salcatech.nfc_api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    public AuthController(GoogleAuthService googleAuthService, JwtService jwtService) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    @GetMapping("/callback")
    public ResponseEntity<?> googleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String authuser,
            @RequestParam(required = false) String prompt,
            HttpServletResponse response
    ) {
        logger.info("🔵 Google OAuth callback başladı - Code: {}", code.substring(0, Math.min(10, code.length())) + "...");
        
        try {
            Map<String, Object> userInfo = googleAuthService.getUserInfoFromCode(code);
            logger.info("🔵 Google'dan kullanıcı bilgileri alındı: {}", userInfo);
            
            String email = (String) userInfo.get("email");
            logger.info("🔵 Email: {}", email);

            String jwt = jwtService.generateTokenWithUserInfo(userInfo);
            logger.info("🔵 JWT token oluşturuldu, uzunluk: {}", jwt.length());

            // JWT'yi HttpOnly Cookie'ye koy
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Development için false, production'da true yap
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 saat

            response.addCookie(cookie);
            logger.info("🔵 Cookie eklendi: jwt={}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

            // Frontend'e sade user info dön
            logger.info("🔵 Frontend'e user info dönülüyor: {}", userInfo);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            logger.error("🔴 Google OAuth callback hatası: ", e);
            return ResponseEntity.status(500).body("OAuth callback error: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpServletRequest request) {
        logger.info("🔵 /auth/me endpoint'i çağrıldı");
        
        // Cookie'den JWT token'ı al
        String jwt = null;
        if (request.getCookies() != null) {
            logger.info("🔵 Gelen cookies sayısı: {}", request.getCookies().length);
            for (Cookie cookie : request.getCookies()) {
                logger.info("🔵 Cookie: {} = {}", cookie.getName(), cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "..." : "null");
            }
            
            jwt = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        } else {
            logger.warn("🔴 Hiç cookie gelmedi!");
        }

        if (jwt == null) {
            logger.warn("🔴 JWT token bulunamadı!");
            return ResponseEntity.status(401).body("Unauthorized - No JWT token");
        }
        
        logger.info("🔵 JWT token bulundu: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
        
        if (!jwtService.validateToken(jwt)) {
            logger.warn("🔴 JWT token geçersiz!");
            return ResponseEntity.status(401).body("Unauthorized - Invalid JWT token");
        }

        try {
            // JWT'den kullanıcı bilgilerini çıkar
            String email = jwtService.extractEmail(jwt);
            String name = jwtService.extractName(jwt);
            String picture = jwtService.extractPicture(jwt);
            
            logger.info("🔵 JWT'den çıkarılan bilgiler - Email: {}, Name: {}, Picture: {}", email, name, picture);
            
            Map<String, Object> userInfo = Map.of(
                "email", email,
                "name", name,
                "picture", picture
            );
            
            logger.info("🔵 Frontend'e dönülen user info: {}", userInfo);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            logger.error("🔴 JWT'den bilgi çıkarma hatası: ", e);
            return ResponseEntity.status(500).body("Error extracting user info: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Cookie'yi sil
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Hemen sil
        response.addCookie(cookie);
        
        return ResponseEntity.ok("Logged out successfully");
    }
}
