package com.salcatech.nfc_api.util;

import com.salcatech.nfc_api.dto.GoogleUserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtAuthenticationUtil {
    public static GoogleUserInfo getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfo user) {
            return user;
        }

        return null;
    }

    public static String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfo user) {
            return user.getAccessToken();
        }

        return null;
    }

    public static String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfo user) {
            return user.getEmail();
        }

        return null;
    }

    public static String getAccessToken(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof GoogleUserInfo user) {
            return user.getAccessToken();
        }
        return null;
    }
}
