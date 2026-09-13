package com.finance.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            AuditLogService auditLogService) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
    }

    public Wallet getWalletByUserId(Long userId) {

        return walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));
    }

    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
                }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId(
                UUID.randomUUID().toString()
        );

        transactionRepository.save(transaction);

        auditLogService.log(
                wallet.getUser(),
                "DEPOSIT",
                "amount=" + amount
        );

        return wallet;
    }

    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
                }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId(
                UUID.randomUUID().toString()
        );

        transactionRepository.save(transaction);

        auditLogService.log(
                wallet.getUser(),
                "WITHDRAWAL",
                "amount=" + amount
        );

        return wallet;
    }


    
}