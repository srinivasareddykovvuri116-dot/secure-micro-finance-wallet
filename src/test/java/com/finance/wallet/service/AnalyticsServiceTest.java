package com.finance.wallet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finance.wallet.dto.AnalyticsResponse;
import com.finance.wallet.dto.AnalyticsTrendResponse;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setBalance(new BigDecimal("5000.00"));
    }

    @Test
    void getAnalyticsShouldReturnCorrectAnalytics() {

        Long userId = 1L;

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.sumAmountByUserIdAndTypeAndStatus(
                userId,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS))
                .thenReturn(new BigDecimal("10000.00"));

        when(transactionRepository.sumAmountByUserIdAndTypeAndStatus(
                userId,
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS))
                .thenReturn(new BigDecimal("2000.00"));

        when(transactionRepository.sumAmountByUserIdAndTypeAndStatus(
                userId,
                TransactionType.TRANSFER_SENT,
                TransactionStatus.SUCCESS))
                .thenReturn(new BigDecimal("1500.00"));

        when(transactionRepository.sumAmountByUserIdAndTypeAndStatus(
                userId,
                TransactionType.TRANSFER_RECEIVED,
                TransactionStatus.SUCCESS))
                .thenReturn(new BigDecimal("3000.00"));

        when(transactionRepository.countByUserIdAndTypeAndStatus(
                userId,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS))
                .thenReturn(5L);

        when(transactionRepository.countByUserIdAndTypeAndStatus(
                userId,
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESS))
                .thenReturn(2L);

        when(transactionRepository.countByUserIdAndTypeAndStatus(
                userId,
                TransactionType.TRANSFER_SENT,
                TransactionStatus.SUCCESS))
                .thenReturn(3L);

        when(transactionRepository.countByUserIdAndTypeAndStatus(
                userId,
                TransactionType.TRANSFER_RECEIVED,
                TransactionStatus.SUCCESS))
                .thenReturn(4L);

        AnalyticsResponse response =
                analyticsService.getAnalytics(userId);

        assertEquals(new BigDecimal("5000.00"), response.getCurrentBalance());
        assertEquals(new BigDecimal("10000.00"), response.getTotalDeposits());
        assertEquals(new BigDecimal("2000.00"), response.getTotalWithdrawals());
        assertEquals(new BigDecimal("1500.00"), response.getTotalSent());
        assertEquals(new BigDecimal("3000.00"), response.getTotalReceived());

        assertEquals(5L, response.getDepositCount());
        assertEquals(2L, response.getWithdrawalCount());
        assertEquals(3L, response.getSentCount());
        assertEquals(4L, response.getReceivedCount());
    }

    @Test
    void getAnalyticsShouldThrowWhenWalletDoesNotExist() {

        Long userId = 1L;

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> analyticsService.getAnalytics(userId)
                );

        assertEquals("Wallet not found", exception.getMessage());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getDailyTrendsShouldReturnCorrectTrends() {

        Long userId = 1L;

        LocalDate firstDate = LocalDate.of(2026, 9, 10);
        LocalDate secondDate = LocalDate.of(2026, 9, 11);

        List<Object[]> results = List.of(
                new Object[] {
                        firstDate,
                        new BigDecimal("1000.00"),
                        new BigDecimal("250.00")
                },
                new Object[] {
                        secondDate,
                        new BigDecimal("500.00"),
                        new BigDecimal("100.00")
                }
        );

        when(transactionRepository.findDailyTrends(userId))
                .thenReturn(results);

        List<AnalyticsTrendResponse> response =
                analyticsService.getDailyTrends(userId);

        assertEquals(2, response.size());

        assertEquals(firstDate, response.get(0).getDate());
        assertEquals(
                new BigDecimal("1000.00"),
                response.get(0).getInflow()
        );
        assertEquals(
                new BigDecimal("250.00"),
                response.get(0).getOutflow()
        );

        assertEquals(secondDate, response.get(1).getDate());
        assertEquals(
                new BigDecimal("500.00"),
                response.get(1).getInflow()
        );
        assertEquals(
                new BigDecimal("100.00"),
                response.get(1).getOutflow()
        );

        verify(transactionRepository).findDailyTrends(userId);
    }

    @Test
    void getDailyTrendsShouldReturnEmptyListWhenNoTransactionsExist() {

        Long userId = 1L;

        when(transactionRepository.findDailyTrends(userId))
                .thenReturn(List.of());

        List<AnalyticsTrendResponse> response =
                analyticsService.getDailyTrends(userId);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(transactionRepository).findDailyTrends(userId);
    }
}