package com.finance.wallet.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-for-wallet-tests-12345678901234567890",
                3600000L
        );
    }

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken(1L, "ROLE_USER");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUserId() {

        String token = jwtService.generateToken(1L, "ROLE_USER");

        String userId = jwtService.extractUserId(token);

        assertEquals("1", userId);
    }

    @Test
    void shouldExtractRole() {

        String token = jwtService.generateToken(1L, "ROLE_USER");

        String role = jwtService.extractRole(token);

        assertEquals("ROLE_USER", role);
    }

    @Test
    void shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(1L, "ROLE_USER");

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {

        assertFalse(jwtService.isTokenValid("invalid-token"));
    }
}