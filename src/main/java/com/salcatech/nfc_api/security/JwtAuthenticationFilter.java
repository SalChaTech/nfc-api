package com.salcatech.nfc_api.security;

import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import com.salcatech.nfc_api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${application.jwt.name}")
    private String applicationJWTName;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException {

        try {
            String jwt = null;
            if (request.getCookies() != null) {
                jwt = Arrays.stream(request.getCookies())
                        .filter(c -> applicationJWTName.equals(c.getName()))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
            }

            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(jwt)) {
                    GoogleUserInfoDTO userDetails = new GoogleUserInfoDTO();
                    userDetails.setName(jwtUtil.extractName(jwt));
                    userDetails.setEmail(jwtUtil.extractEmail(jwt));
                    userDetails.setAccessToken(jwtUtil.extractAccessToken(jwt));
                    userDetails.setRole(jwtUtil.extractRole(jwt));

                    var authorities = List.of(new SimpleGrantedAuthority(userDetails.getRole()));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);

                }

            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT filter hatası: ", e);
            throw new ServletException("JWT filter processing error", e);
        }
    }

}
