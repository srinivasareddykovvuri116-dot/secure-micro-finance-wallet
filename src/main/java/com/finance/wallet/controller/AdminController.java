package com.finance.wallet.controller;

import com.finance.wallet.dto.AdminUserResponse;
import com.finance.wallet.entity.User;
import com.finance.wallet.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {

        List<AdminUserResponse> users = adminService.getAllUsers()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<AdminUserResponse> setUserActiveStatus(
            @PathVariable Long userId,
            @RequestParam boolean active,
            Authentication authentication) {

        Long requestingAdminId =
                (Long) authentication.getPrincipal();

        User user = adminService.setUserActiveStatus(
                userId,
                active,
                requestingAdminId
        );

        return ResponseEntity.ok(toResponse(user));
    }

    private AdminUserResponse toResponse(User user) {

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive()
        );
    }
}