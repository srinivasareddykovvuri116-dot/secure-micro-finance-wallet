package com.finance.wallet.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;

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

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.wallet.user.id = :userId
      AND t.type = :type
      AND t.status = :status
    """)
    BigDecimal sumAmountByUserIdAndTypeAndStatus(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status
    );

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.wallet.user.id = :userId
        AND t.type = :type
        AND t.status = :status
    """)
    long countByUserIdAndTypeAndStatus(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status
    );

    @Query(value = """
        SELECT
            CAST(t.created_at AS DATE) AS transaction_date,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type IN ('DEPOSIT', 'TRANSFER_RECEIVED')
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) AS inflow,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type IN ('WITHDRAWAL', 'TRANSFER_SENT')
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) AS outflow
        FROM transactions t
        JOIN wallets w ON t.wallet_id = w.id
        WHERE w.user_id = :userId
        AND t.status = 'SUCCESS'
        GROUP BY CAST(t.created_at AS DATE)
        ORDER BY transaction_date ASC
        """, nativeQuery = true)
    List<Object[]> findDailyTrends(@Param("userId") Long userId);
}