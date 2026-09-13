package com.finance.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.wallet.dto.AnalyticsResponse;
import com.finance.wallet.dto.AnalyticsTrendResponse;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.WalletRepository;

@Service
public class AnalyticsService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public AnalyticsService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(Long userId) {

        BigDecimal currentBalance = walletRepository
                .findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"))
                .getBalance();

        BigDecimal totalDeposits = sum(
                userId,
                TransactionType.DEPOSIT
        );

        BigDecimal totalWithdrawals = sum(
                userId,
                TransactionType.WITHDRAWAL
        );

        BigDecimal totalSent = sum(
                userId,
                TransactionType.TRANSFER_SENT
        );

        BigDecimal totalReceived = sum(
                userId,
                TransactionType.TRANSFER_RECEIVED
        );

        long depositCount = count(
                userId,
                TransactionType.DEPOSIT
        );

        long withdrawalCount = count(
                userId,
                TransactionType.WITHDRAWAL
        );

        long sentCount = count(
                userId,
                TransactionType.TRANSFER_SENT
        );

        long receivedCount = count(
                userId,
                TransactionType.TRANSFER_RECEIVED
        );

        return new AnalyticsResponse(
                currentBalance,
                totalDeposits,
                totalWithdrawals,
                totalSent,
                totalReceived,
                depositCount,
                withdrawalCount,
                sentCount,
                receivedCount
        );
    }

    private BigDecimal sum(
            Long userId,
            TransactionType type) {

        return transactionRepository.sumAmountByUserIdAndTypeAndStatus(
                userId,
                type,
                TransactionStatus.SUCCESS
        );
    }

    private long count(
            Long userId,
            TransactionType type) {

        return transactionRepository.countByUserIdAndTypeAndStatus(
                userId,
                type,
                TransactionStatus.SUCCESS
        );
    }


    @Transactional(readOnly = true)
        public List<AnalyticsTrendResponse> getDailyTrends(Long userId) {

        List<Object[]> results =
                transactionRepository.findDailyTrends(userId);

        List<AnalyticsTrendResponse> trends = new ArrayList<>();

        for (Object[] row : results) {

                trends.add(
                        new AnalyticsTrendResponse(
                                (LocalDate) row[0],
                                (BigDecimal) row[1],
                                (BigDecimal) row[2]
                        )
                );
        }

        return trends;
        }
}