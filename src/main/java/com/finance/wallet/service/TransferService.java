package com.finance.wallet.service;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    public TransferService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public String transfer(
            Long senderUserId,
            String receiverEmail,
            BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }

        Long receiverUserId = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Receiver not found"))
                .getId();

        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to yourself");
        }

        Long firstUserId = Math.min(senderUserId, receiverUserId);
        Long secondUserId = Math.max(senderUserId, receiverUserId);

        Wallet firstWallet =
                walletRepository.findByUserIdForUpdate(firstUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Wallet not found"));

        Wallet secondWallet =
                walletRepository.findByUserIdForUpdate(secondUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Wallet not found"));

        Wallet senderWallet;
        Wallet receiverWallet;

        if (senderUserId.equals(firstUserId)) {
            senderWallet = firstWallet;
            receiverWallet = secondWallet;
        } else {
            senderWallet = secondWallet;
            receiverWallet = firstWallet;
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance");
        }

        senderWallet.setBalance(
                senderWallet.getBalance().subtract(amount));

        receiverWallet.setBalance(
                receiverWallet.getBalance().add(amount));

        String referenceId = UUID.randomUUID().toString();

        Transaction sentTransaction = new Transaction();

        sentTransaction.setWallet(senderWallet);
        sentTransaction.setType(TransactionType.TRANSFER_SENT);
        sentTransaction.setAmount(amount);
        sentTransaction.setStatus(TransactionStatus.SUCCESS);
        sentTransaction.setReferenceId(referenceId);

        Transaction receivedTransaction = new Transaction();

        receivedTransaction.setWallet(receiverWallet);
        receivedTransaction.setType(
                TransactionType.TRANSFER_RECEIVED);
        receivedTransaction.setAmount(amount);
        receivedTransaction.setStatus(TransactionStatus.SUCCESS);
        receivedTransaction.setReferenceId(referenceId);

        transactionRepository.save(sentTransaction);
        transactionRepository.save(receivedTransaction);

        auditLogService.log(
                senderWallet.getUser(),
                "TRANSFER",
                "amount=" + amount
                        + ", receiver=" + receiverEmail
        );

        return referenceId;
    }
}