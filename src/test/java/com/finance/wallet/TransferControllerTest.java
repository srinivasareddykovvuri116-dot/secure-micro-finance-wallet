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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finance.wallet.controller.TransferController;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.TransferService;


@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void transferShouldReturnBadRequestWhenAmountIsZero() throws Exception {

        mockMvc.perform(
                post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "receiverEmail": "receiver@example.com",
                                    "amount": 0
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
    }


    @Test
    void transferShouldReturnSuccessWhenRequestIsValid() throws Exception {

        when(transferService.transfer(
                1L,
                "receiver@example.com",
                new BigDecimal("50.00")
        )).thenReturn("test-reference-123");

        mockMvc.perform(
                post("/api/transfers")
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
                                    "receiverEmail": "receiver@example.com",
                                    "amount": 50.00
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(
                "Transfer successful. Reference ID: test-reference-123"
        ));

        verify(transferService).transfer(
                1L,
                "receiver@example.com",
                new BigDecimal("50.00")
        );
    }
}