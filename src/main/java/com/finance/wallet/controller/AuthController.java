package com.finance.wallet.controller;

import com.finance.wallet.dto.LoginRequest;
import com.finance.wallet.dto.RegistrationRequest;
import com.finance.wallet.entity.User;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegistrationRequest request) {

        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully with ID: " + user.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(
                user.getId(),
                user.getRole()
        );

        return ResponseEntity.ok(token);
    }
}