package com.finance.wallet.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleIllegalArgumentException() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Insufficient balance");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Insufficient balance",
                response.getBody().get("message")
        );
    }


    @Test
    void shouldHandleResourceNotFoundException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("User not found");

        ResponseEntity<Map<String, String>> response =
                handler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "User not found",
                response.getBody().get("message")
        );
    }
}