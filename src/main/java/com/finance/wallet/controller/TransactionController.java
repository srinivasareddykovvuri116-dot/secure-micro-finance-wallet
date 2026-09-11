package com.finance.wallet.controller;

import com.finance.wallet.dto.TransactionResponse;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.service.TransactionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public Page<TransactionResponse> getTransactions(
            Authentication authentication,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Long userId = Long.valueOf(authentication.getName());

        return transactionService.getTransactionsByUserId(
            userId,
            type,
            status,
            pageable
        );
    }
}