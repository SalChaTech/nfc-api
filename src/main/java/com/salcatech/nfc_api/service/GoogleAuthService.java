package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.dto.GoogleUserInfo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Getter
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Google OAuth code -> user info + access token
     */
    public GoogleUserInfo getUserInfoFromCode(String code) {
        String accessToken = getAccessTokenFromCode(code);
        GoogleUserInfo userInfo = getUserInfoFromAccessToken(accessToken);
        userInfo.setAccessToken(accessToken);
        return userInfo;
    }

    /**
     * OAuth code -> Access Token
     */
    private String getAccessTokenFromCode(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(
                "https://oauth2.googleapis.com/token",
                request,
                Map.class
        );

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Google access token alınamadı.");
        }

        return (String) tokenResponse.get("access_token");
    }

    /**
     * Access token -> User info
     */
    private GoogleUserInfo getUserInfoFromAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo", // ✅ OpenID standard endpoint
                HttpMethod.GET,
                request,
                GoogleUserInfo.class
        );

        GoogleUserInfo userInfo = response.getBody();
        if (userInfo == null) {
            throw new RuntimeException("Google user info alınamadı.");
        }

        return userInfo;
    }

}
