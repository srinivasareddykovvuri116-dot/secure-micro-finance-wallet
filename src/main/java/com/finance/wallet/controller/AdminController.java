package com.finance.wallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/api/admin")
    public String adminOnly() {
        return "ADMIN access granted";
    }
}