package com.salcatech.nfc_api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RedirectUriChecker implements CommandLineRunner {

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==== GOOGLE REDIRECT URI ====");
        System.out.println("Redirect URI used by Spring: " + redirectUri);
        System.out.println("============================");
    }
}
