package com.finance.wallet.controller;

import com.finance.wallet.entity.User;
import com.finance.wallet.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<String> setUserActiveStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        User user = adminService.setUserActiveStatus(userId, active);

        String status = user.isActive() ? "activated" : "suspended";

        return ResponseEntity.ok(
                "User " + user.getId() + " " + status + " successfully"
        );
    }
}