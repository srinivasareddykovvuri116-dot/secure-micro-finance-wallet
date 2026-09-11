package com.finance.wallet.repository;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByWalletUserId(
            Long userId,
            Pageable pageable
    );

    Page<Transaction> findByWalletUserIdAndType(
            Long userId,
            TransactionType type,
            Pageable pageable
    );

    Page<Transaction> findByWalletUserIdAndStatus(
        Long userId,
        TransactionStatus status,
        Pageable pageable
    );
    Page<Transaction> findByWalletUserIdAndTypeAndStatus(
        Long userId,
        TransactionType type,
        TransactionStatus status,
        Pageable pageable
    );
}