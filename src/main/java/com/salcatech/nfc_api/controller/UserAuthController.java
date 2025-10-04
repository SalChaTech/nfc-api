package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.UserAuthRequest;
import com.salcatech.nfc_api.dto.UserAuthResponse;
import com.salcatech.nfc_api.model.Users;
import com.salcatech.nfc_api.service.JwtService;
import com.salcatech.nfc_api.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/userAuth")
public class UserAuthController {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthController.class);

    private final UsersService usersService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAuthController(UsersService usersService, JwtService jwtService) {
        this.usersService = usersService;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();

    }


    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody UserAuthRequest req) {


        logger.info("productId: {} password : {}", req.getProductId(), req.getPassword());

        if (req.getProductId() == null || req.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ProductId & Password Required!");
        }

        Users user = usersService.getUserByProductId(req.getProductId());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
        }

        String storedHash = user.getPasswordHash();
        if (storedHash == null || !passwordEncoder.matches(req.getPassword(), storedHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password Incorrect!");

        }


        String token = jwtService.generateTokenByProductIdAndUserId(req.getProductId(), user.getId());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ProductId & Password!");
        }

        return ResponseEntity.ok(new UserAuthResponse(token));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenRequest request) {
        logger.info("token info"+request.getToken());
        boolean valid = jwtService.validateToken(request.getToken());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    private static class TokenRequest {
        private String token;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
