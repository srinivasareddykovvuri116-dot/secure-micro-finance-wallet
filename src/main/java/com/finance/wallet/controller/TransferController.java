package com.finance.wallet.controller;

import com.finance.wallet.dto.TransferRequest;
import com.finance.wallet.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        Long senderUserId = Long.valueOf(authentication.getName());

        String referenceId = transferService.transfer(
                senderUserId,
                request.getReceiverEmail(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                "Transfer successful. Reference ID: " + referenceId
        );
    }
}