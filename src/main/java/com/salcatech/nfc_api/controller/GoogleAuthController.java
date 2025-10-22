package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.GoogleUserInfo;
import com.salcatech.nfc_api.dto.response.HandleGoogleCallbackResponse;
import com.salcatech.nfc_api.dto.request.ValidateTokenRequest;
import com.salcatech.nfc_api.service.GoogleAuthService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import com.salcatech.nfc_api.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    @Value("${application.jwt.name}")
    private String applicationJWTName;

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @GetMapping("/login")
    public void startGoogleLogin(HttpServletResponse response) throws IOException {
        String googleOauthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + googleAuthService.getClientId() +
                "&redirect_uri=" + googleAuthService.getRedirectUri() +   // örn: http://localhost:8080/auth/callback
                "&response_type=code" +
                "&scope=openid email profile https://www.googleapis.com/auth/drive.file";
        response.sendRedirect(googleOauthUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<HandleGoogleCallbackResponse>> handleGoogleCallback(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        logger.info("🔵 Google OAuth callback başladı - Code: {}", code.substring(0, Math.min(10, code.length())) + "...");

        try {
            GoogleUserInfo userInfo = googleAuthService.getUserInfoFromCode(code);
            logger.info("🔵 Google'dan kullanıcı bilgileri alındı: {}", userInfo);

            String email = userInfo.getEmail();
            logger.info("🔵 Email: {}", email);

            String jwt = JwtUtil.generateTokenWithUserInfo(userInfo);
            logger.info("🔵 JWT token oluşturuldu, uzunluk: {}", jwt.length());

            Cookie cookie = new Cookie(applicationJWTName, jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Development için false, production'da true yap
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 saat

            response.addCookie(cookie);
            logger.info("🔵 Cookie eklendi: jwt={}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

            ApiResponse<HandleGoogleCallbackResponse> apiResponse = new ApiResponse<>(true, "Handle google callback succeed!", new HandleGoogleCallbackResponse(jwt, userInfo));
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("🔴 Google OAuth callback hatası: ", e);
            ApiResponse<HandleGoogleCallbackResponse> apiResponse = new ApiResponse<>(false, "OAuth callback error: " + e.getMessage(), null);

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GoogleUserInfo>> getCurrentUser() {
        logger.info("🔵 /auth/me endpoint'i çağrıldı");

        GoogleUserInfo userInfo = JwtAuthenticationUtil.getUserInfo();

        logger.info("🔵 Frontend'e dönülen user info: {}", userInfo);

        ApiResponse<GoogleUserInfo> apiResponse = new ApiResponse<>(true, "getCurrentUser succeed!", userInfo);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(applicationJWTName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(new ApiResponse<>(true, "Logout succeed!", null));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestBody ValidateTokenRequest request) {
        boolean valid = JwtUtil.validateToken(request.getToken());
        return ResponseEntity.ok(new ApiResponse<>(true, "Validate token succeed!", valid ? "Valid" : "Invalid"));
    }


}
