package com.salcatech.nfc_api;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class RandomTest {
    @Test
    public void test() {
        String out = new BCryptPasswordEncoder().encode("123456");
        System.out.println(out);
    }
}
