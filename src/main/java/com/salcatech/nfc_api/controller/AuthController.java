package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.service.GoogleAuthService;
import com.salcatech.nfc_api.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    public AuthController(GoogleAuthService googleAuthService, JwtService jwtService) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    @GetMapping("/callback")
    public ResponseEntity<?> googleCallback(@RequestParam String code,@RequestParam(required = false) String scope,
                                            @RequestParam(required = false) String authuser,
                                            @RequestParam(required = false) String prompt) {
        Map<String, Object> userInfo = googleAuthService.getUserInfoFromCode(code);
        String email = (String) userInfo.get("email");

        String jwt = jwtService.generateToken(email);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userInfo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        return ResponseEntity.ok("Giriş yapan kullanıcı: " + authentication.getName());
    }
}
