package com.salcatech.nfc_api.security;

import com.salcatech.nfc_api.dto.GoogleUserInfo;
import com.salcatech.nfc_api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${application.jwt.name}")
    private String applicationJWTName;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwt = null;
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(c -> applicationJWTName.equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (JwtUtil.validateToken(jwt)) {
                GoogleUserInfo userDetails = new GoogleUserInfo();
                userDetails.setName(JwtUtil.extractName(jwt));
                userDetails.setEmail(JwtUtil.extractEmail(jwt));
                userDetails.setAccessToken(JwtUtil.extractAccessToken(jwt));
                userDetails.setRole(JwtUtil.extractRole(jwt));

                var authorities = List.of(new SimpleGrantedAuthority(userDetails.getRole()));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

            }

        }

        filterChain.doFilter(request, response);
    }
}
