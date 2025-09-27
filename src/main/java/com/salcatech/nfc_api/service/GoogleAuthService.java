package com.salcatech.nfc_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getUserInfoFromCode(String code) {
        // 1) Access token almak için parametreler
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        // 2) Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 3) Token isteği
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restTemplate.postForObject(
                "https://oauth2.googleapis.com/token",
                request,
                Map.class
        );

        if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
            throw new RuntimeException("Google access token alınamadı.");
        }

        String accessToken = (String) tokenResponse.get("access_token");

        // 4) User info isteği
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

        @SuppressWarnings({"unchecked", "rawtypes"})
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userEntity,
                Map.class
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = userInfoResponse.getBody();
        return userInfo;
    }
}
