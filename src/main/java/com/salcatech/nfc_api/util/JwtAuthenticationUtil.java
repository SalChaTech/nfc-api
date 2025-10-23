package com.salcatech.nfc_api.util;

import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationUtil {
    public static GoogleUserInfoDTO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfoDTO user) {
            return user;
        }

        return null;
    }

    public static String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfoDTO user) {
            return user.getAccessToken();
        }

        return null;
    }

    public static String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfoDTO user) {
            return user.getEmail();
        }

        return null;
    }

}
