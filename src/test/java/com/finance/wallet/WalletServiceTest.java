package com.finance.wallet;

import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.TransactionRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.AuditLogService;
import com.finance.wallet.service.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private TransactionRepository transactionRepository;
    private AuditLogService auditLogService;

    private WalletService walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {

        walletRepository = mock(WalletRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        auditLogService = mock(AuditLogService.class);

        walletService = new WalletService(
                walletRepository,
                transactionRepository,
                auditLogService
        );

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole("ROLE_USER");
        user.setActive(true);

        wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("100.00"));
    }

    @Test
    void depositShouldIncreaseBalanceAndCreateTransaction() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        Wallet result = walletService.deposit(
                1L,
                new BigDecimal("50.00")
        );

        assertEquals(
                new BigDecimal("150.00"),
                result.getBalance()
        );

        verify(walletRepository).save(wallet);

        verify(transactionRepository)
                .save(any(Transaction.class));

        verify(auditLogService).log(
                user,
                "DEPOSIT",
                "amount=50.00"
        );
    }

    @Test
    void withdrawShouldDecreaseBalanceAndCreateTransaction() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        Wallet result = walletService.withdraw(
                1L,
                new BigDecimal("40.00")
        );

        assertEquals(
                new BigDecimal("60.00"),
                result.getBalance()
        );

        verify(walletRepository).save(wallet);

        verify(transactionRepository)
                .save(any(Transaction.class));

        verify(auditLogService).log(
                user,
                "WITHDRAWAL",
                "amount=40.00"
        );
    }

    @Test
    void withdrawShouldFailWhenBalanceIsInsufficient() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.withdraw(
                        1L,
                        new BigDecimal("150.00")
                )
        );

        assertEquals(
                new BigDecimal("100.00"),
                wallet.getBalance()
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void depositShouldFailWhenWalletDoesNotExist() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> walletService.deposit(
                        1L,
                        new BigDecimal("50.00")
                )
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void withdrawShouldFailWhenWalletDoesNotExist() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> walletService.withdraw(
                        1L,
                        new BigDecimal("50.00")
                )
        );

        verify(walletRepository, never())
                .save(any(Wallet.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(auditLogService, never())
                .log(any(), anyString(), anyString());
    }

    @Test
    void depositShouldCreateSuccessfulDepositTransaction() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        walletService.deposit(
                1L,
                new BigDecimal("25.00")
        );

        verify(transactionRepository).save(
                argThat(transaction ->
                        transaction.getWallet() == wallet
                                && transaction.getType()
                                == TransactionType.DEPOSIT
                                && transaction.getAmount()
                                .compareTo(new BigDecimal("25.00")) == 0
                                && transaction.getStatus()
                                == TransactionStatus.SUCCESS
                                && transaction.getReferenceId() != null
                )
        );
    }

    @Test
    void withdrawShouldCreateSuccessfulWithdrawalTransaction() {

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        walletService.withdraw(
                1L,
                new BigDecimal("25.00")
        );

        verify(transactionRepository).save(
                argThat(transaction ->
                        transaction.getWallet() == wallet
                                && transaction.getType()
                                == TransactionType.WITHDRAWAL
                                && transaction.getAmount()
                                .compareTo(new BigDecimal("25.00")) == 0
                                && transaction.getStatus()
                                == TransactionStatus.SUCCESS
                                && transaction.getReferenceId() != null
                )
        );
    }

    @Test
    void getWalletShouldReturnWalletWhenWalletExists() {

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletByUserId(1L);

        assertSame(wallet, result);

        verify(walletRepository)
                .findByUserId(1L);
    }

    @Test
    void getWalletShouldFailWhenWalletDoesNotExist() {

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> walletService.getWalletByUserId(1L)
        );

        verify(walletRepository)
                .findByUserId(1L);
    }

    @Test
    void depositShouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.deposit(
                        1L,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(
                walletRepository,
                transactionRepository,
                auditLogService
        );
    }

    @Test
    void withdrawShouldRejectNegativeAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> walletService.withdraw(
                        1L,
                        new BigDecimal("-10.00")
                )
        );

        verifyNoInteractions(
                walletRepository,
                transactionRepository,
                auditLogService
        );
    }
}