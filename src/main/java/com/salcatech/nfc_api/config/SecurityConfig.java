package com.salcatech.nfc_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API için CSRF kapalı
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/oauth2/code/google").permitAll() // Google login endpoint serbest
                        .anyRequest().permitAll() // Geliştirme aşaması için hepsi serbest
                )
                .formLogin(form -> form.disable())
                .oauth2Login(oauth -> oauth.disable());

        return http.build();
    }
}
