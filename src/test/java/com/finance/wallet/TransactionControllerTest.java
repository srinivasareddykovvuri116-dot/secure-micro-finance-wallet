package com.finance.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finance.wallet.controller.TransactionController;
import com.finance.wallet.dto.TransactionResponse;
import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.TransactionService;


@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void getTransactionsShouldReturnUserTransactions() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS,
                "test-reference-123",
                Instant.now()
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transaction)
                );

        when(transactionService.getTransactionsByUserId(
                eq(1L),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/transactions")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.content[0].amount").value(100.00))
        .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$.content[0].referenceId")
                .value("test-reference-123"));
    }


    @Test
    void getTransactionsShouldFilterByType() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS,
                "deposit-ref-123",
                Instant.now()
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transaction)
                );

        when(transactionService.getTransactionsByUserId(
                eq(1L),
                eq(TransactionType.DEPOSIT),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/transactions")
                        .param("type", "DEPOSIT")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }


    @Test
    void getTransactionsShouldFilterByStatus() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS,
                "success-ref-123",
                Instant.now()
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transaction)
                );

        when(transactionService.getTransactionsByUserId(
                eq(1L),
                isNull(),
                eq(TransactionStatus.SUCCESS),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/transactions")
                        .param("status", "SUCCESS")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }


    @Test
    void getTransactionsShouldFilterByTypeAndStatus() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                TransactionStatus.SUCCESS,
                "combined-ref-123",
                Instant.now()
        );

        PageImpl<TransactionResponse> page =
                new PageImpl<>(
                        List.of(transaction)
                );

        when(transactionService.getTransactionsByUserId(
                eq(1L),
                eq(TransactionType.DEPOSIT),
                eq(TransactionStatus.SUCCESS),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/transactions")
                        .param("type", "DEPOSIT")
                        .param("status", "SUCCESS")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }
}