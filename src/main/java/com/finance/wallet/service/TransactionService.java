package com.finance.wallet.service;

import com.finance.wallet.dto.TransactionResponse;
import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;

import com.finance.wallet.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.finance.wallet.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionResponse> getTransactionsByUserId(
            Long userId,
            TransactionType type,
            TransactionStatus status,
            Pageable pageable) {

        Page<Transaction> transactions;

        if (type == null && status == null) {

            transactions = transactionRepository.findByWalletUserId(
                    userId,
                    pageable
            );

        } else if (type != null && status == null) {

            transactions = transactionRepository.findByWalletUserIdAndType(
                    userId,
                    type,
                    pageable
            );

        } else if (type == null) {

            transactions = transactionRepository.findByWalletUserIdAndStatus(
                    userId,
                    status,
                    pageable
            );

        } else {

            transactions = transactionRepository.findByWalletUserIdAndTypeAndStatus(
                    userId,
                    type,
                    status,
                    pageable
            );
        }

        return transactions.map(transaction -> new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getReferenceId(),
                transaction.getCreatedAt()
        ));
    }
}