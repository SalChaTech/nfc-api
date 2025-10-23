package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import com.salcatech.nfc_api.exception.NotFetchedAccessTokenFromCode;
import com.salcatech.nfc_api.exception.NotFetchedUserInfoFromAccessCode;
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
    public GoogleUserInfoDTO getUserInfoFromCode(String code) throws NotFetchedUserInfoFromAccessCode, NotFetchedAccessTokenFromCode {
        String accessToken = getAccessTokenFromCode(code);
        GoogleUserInfoDTO userInfo = getUserInfoFromAccessToken(accessToken);
        userInfo.setAccessToken(accessToken);
        return userInfo;
    }

    /**
     * OAuth code -> Access Token
     */
    private String getAccessTokenFromCode(String code) throws NotFetchedAccessTokenFromCode {
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
            throw new NotFetchedAccessTokenFromCode();
        }

        return (String) tokenResponse.get("access_token");
    }

    /**
     * Access token -> User info
     */
    private GoogleUserInfoDTO getUserInfoFromAccessToken(String accessToken) throws NotFetchedUserInfoFromAccessCode {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfoDTO> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo", // ✅ OpenID standard endpoint
                HttpMethod.GET,
                request,
                GoogleUserInfoDTO.class
        );

        GoogleUserInfoDTO userInfo = response.getBody();
        if (userInfo == null) {
            throw new NotFetchedUserInfoFromAccessCode();
        }

        return userInfo;
    }

}
