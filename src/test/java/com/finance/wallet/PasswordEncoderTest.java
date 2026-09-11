package com.finance.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void shouldHashAndVerifyPassword() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "hello123";

        String hashedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, hashedPassword);

        assertTrue(
            encoder.matches(rawPassword, hashedPassword)
        );

        assertFalse(
            encoder.matches("wrongPassword", hashedPassword)
        );
    }
}