package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import com.salcatech.nfc_api.dto.response.HandleGoogleCallbackResponse;
import com.salcatech.nfc_api.dto.request.ValidateTokenRequest;
import com.salcatech.nfc_api.exception.InvalidJwtException;
import com.salcatech.nfc_api.exception.NotFetchedAccessTokenFromCode;
import com.salcatech.nfc_api.exception.NotFetchedUserInfoFromAccessCode;
import com.salcatech.nfc_api.service.GoogleAuthService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import com.salcatech.nfc_api.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    @Value("${application.jwt.name}")
    private String applicationJWTName;

    private final GoogleAuthService googleAuthService;

    @Autowired
    private JwtUtil jwtUtil;

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
    ) throws NotFetchedAccessTokenFromCode, NotFetchedUserInfoFromAccessCode {
        logger.info("🔵 Google OAuth callback başladı - Code: {}", code.substring(0, Math.min(10, code.length())) + "...");

        GoogleUserInfoDTO userInfo = googleAuthService.getUserInfoFromCode(code);

        logger.info("🔵 Google'dan kullanıcı bilgileri alındı: {}", userInfo);

        String email = userInfo.getEmail();
        logger.info("🔵 Email: {}", email);

        String jwt = jwtUtil.generateTokenWithUserInfo(userInfo);
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

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GoogleUserInfoDTO>> getCurrentUser() {
        logger.info("🔵 /auth/me endpoint'i çağrıldı");

        GoogleUserInfoDTO userInfo = JwtAuthenticationUtil.getUserInfo();

        logger.info("🔵 Frontend'e dönülen user info: {}", userInfo);

        ApiResponse<GoogleUserInfoDTO> apiResponse = new ApiResponse<>(true, "getCurrentUser succeed!", userInfo);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("isAuthenticated()")
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
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestBody ValidateTokenRequest request) throws InvalidJwtException {
        boolean valid = jwtUtil.validateToken(request.getToken());
        return ResponseEntity.ok(new ApiResponse<>(true, "Validate token succeed!", valid ? "Valid" : "Invalid"));
    }


}
