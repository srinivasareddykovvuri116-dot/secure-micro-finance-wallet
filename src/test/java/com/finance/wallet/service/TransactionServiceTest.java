package com.finance.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.finance.wallet.dto.TransactionResponse;
import com.finance.wallet.entity.Transaction;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();

        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("ref-123");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldReturnTransactionsWhenNoFiltersProvided() {

        Long userId = 1L;

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findByWalletUserId(userId, pageable))
                .thenReturn(page);

        Page<TransactionResponse> response =
                transactionService.getTransactionsByUserId(
                        userId,
                        null,
                        null,
                        pageable
                );

        assertEquals(1, response.getTotalElements());

        TransactionResponse result =
                response.getContent().get(0);

        assertEquals(
                TransactionType.DEPOSIT,
                result.getType()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                result.getAmount()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                "ref-123",
                result.getReferenceId()
        );

        verify(transactionRepository)
                .findByWalletUserId(userId, pageable);

        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void shouldFilterByTypeOnly() {

        Long userId = 1L;

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findByWalletUserIdAndType(
                userId,
                TransactionType.DEPOSIT,
                pageable))
                .thenReturn(page);

        Page<TransactionResponse> response =
                transactionService.getTransactionsByUserId(
                        userId,
                        TransactionType.DEPOSIT,
                        null,
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                response.getContent().get(0).getType()
        );

        verify(transactionRepository)
                .findByWalletUserIdAndType(
                        userId,
                        TransactionType.DEPOSIT,
                        pageable
                );

        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void shouldFilterByStatusOnly() {

        Long userId = 1L;

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findByWalletUserIdAndStatus(
                userId,
                TransactionStatus.SUCCESS,
                pageable))
                .thenReturn(page);

        Page<TransactionResponse> response =
                transactionService.getTransactionsByUserId(
                        userId,
                        null,
                        TransactionStatus.SUCCESS,
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getContent().get(0).getStatus()
        );

        verify(transactionRepository)
                .findByWalletUserIdAndStatus(
                        userId,
                        TransactionStatus.SUCCESS,
                        pageable
                );

        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void shouldFilterByTypeAndStatus() {

        Long userId = 1L;

        Page<Transaction> page =
                new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findByWalletUserIdAndTypeAndStatus(
                userId,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                pageable))
                .thenReturn(page);

        Page<TransactionResponse> response =
                transactionService.getTransactionsByUserId(
                        userId,
                        TransactionType.DEPOSIT,
                        TransactionStatus.SUCCESS,
                        pageable
                );

        assertEquals(
                1,
                response.getTotalElements()
        );

        TransactionResponse result =
                response.getContent().get(0);

        assertEquals(
                TransactionType.DEPOSIT,
                result.getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                result.getStatus()
        );

        verify(transactionRepository)
                .findByWalletUserIdAndTypeAndStatus(
                        userId,
                        TransactionType.DEPOSIT,
                        TransactionStatus.SUCCESS,
                        pageable
                );

        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void shouldReturnEmptyPageWhenNoTransactionsExist() {

        Long userId = 1L;

        Page<Transaction> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(transactionRepository.findByWalletUserId(
                userId,
                pageable))
                .thenReturn(emptyPage);

        Page<TransactionResponse> response =
                transactionService.getTransactionsByUserId(
                        userId,
                        null,
                        null,
                        pageable
                );

        assertEquals(
                0,
                response.getTotalElements()
        );

        assertEquals(
                0,
                response.getContent().size()
        );

        verify(transactionRepository)
                .findByWalletUserId(userId, pageable);
    }
}