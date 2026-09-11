package com.finance.wallet.service;

import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.exception.ResourceNotFoundException;

import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));
    }

    @Transactional
    public Wallet deposit(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId(UUID.randomUUID().toString());

        transactionRepository.save(transaction);

        return wallet;
    }

    @Transactional
    public Wallet withdraw(Long userId, BigDecimal amount) {

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId(UUID.randomUUID().toString());

        transactionRepository.save(transaction);

        return wallet;
    }
}