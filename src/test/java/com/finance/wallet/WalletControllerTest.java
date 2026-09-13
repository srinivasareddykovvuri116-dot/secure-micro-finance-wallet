package com.finance.wallet;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finance.wallet.controller.WalletController;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.WalletService;


@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void getWalletShouldReturnWalletDetails() throws Exception {

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("100.00"));

        when(walletService.getWalletByUserId(1L))
                .thenReturn(wallet);

        mockMvc.perform(
                get("/api/wallet")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(100.00));
    }


    @Test
    void depositShouldReturnUpdatedWallet() throws Exception {

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("150.00"));

        when(walletService.deposit(
                1L,
                new BigDecimal("50.00")
        )).thenReturn(wallet);

        mockMvc.perform(
                post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
                        .content("""
                                {
                                    "amount": 50.00
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(150.00));

        verify(walletService).deposit(
                1L,
                new BigDecimal("50.00")
        );
    }


    @Test
    void withdrawShouldReturnUpdatedWallet() throws Exception {

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("50.00"));

        when(walletService.withdraw(
                1L,
                new BigDecimal("50.00")
        )).thenReturn(wallet);

        mockMvc.perform(
                post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
                        .content("""
                                {
                                    "amount": 50.00
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(50.00));

        verify(walletService).withdraw(
                1L,
                new BigDecimal("50.00")
        );
    }


    @Test
    void withdrawShouldReturnBadRequestWhenAmountIsZero() throws Exception {

        mockMvc.perform(
                post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
                        .content("""
                                {
                                    "amount": 0
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
    }


    @Test
        void depositShouldRejectZeroAmount() throws Exception {

        mockMvc.perform(
                post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                        "amount": 0
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
        }


        @Test
        void depositShouldRejectNegativeAmount() throws Exception {

        mockMvc.perform(
                post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                        "amount": -100
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
        }


        @Test
        void depositShouldRejectMissingAmount() throws Exception {

        mockMvc.perform(
                post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
        }


        @Test
        void withdrawShouldRejectMissingAmount() throws Exception {

        mockMvc.perform(
                post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
        }


}