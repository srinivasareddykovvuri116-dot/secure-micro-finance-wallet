package com.finance.wallet.controller;

import com.finance.wallet.dto.WalletResponse;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.service.WalletService;

import com.finance.wallet.dto.DepositRequest;
import com.finance.wallet.dto.WithdrawRequest;

import jakarta.validation.Valid;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse getWallet(Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        Wallet wallet = walletService.getWalletByUserId(userId);

        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        Wallet wallet = walletService.deposit(
                userId,
                request.getAmount()
        );

        WalletResponse response = new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        Wallet wallet = walletService.withdraw(
                userId,
                request.getAmount()
        );

        WalletResponse response = new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
}