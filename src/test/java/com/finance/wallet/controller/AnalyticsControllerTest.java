package com.finance.wallet.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finance.wallet.dto.AnalyticsResponse;
import com.finance.wallet.dto.AnalyticsTrendResponse;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.AnalyticsService;
import com.finance.wallet.service.JwtService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;


    @Test
    void getAnalyticsShouldReturnAnalytics() throws Exception {

        AnalyticsResponse response = new AnalyticsResponse(
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("400.00"),
                2,
                1,
                1,
                1
        );

        when(analyticsService.getAnalytics(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/analytics")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentBalance").value(1000.00))
        .andExpect(jsonPath("$.totalDeposits").value(1500.00))
        .andExpect(jsonPath("$.totalWithdrawals").value(200.00))
        .andExpect(jsonPath("$.totalSent").value(300.00))
        .andExpect(jsonPath("$.totalReceived").value(400.00))
        .andExpect(jsonPath("$.depositCount").value(2))
        .andExpect(jsonPath("$.withdrawalCount").value(1))
        .andExpect(jsonPath("$.sentCount").value(1))
        .andExpect(jsonPath("$.receivedCount").value(1));
    }


    @Test
    void getDailyTrendsShouldReturnTrends() throws Exception {

        AnalyticsTrendResponse trend = new AnalyticsTrendResponse(
                LocalDate.of(2026, 9, 12),
                new BigDecimal("500.00"),
                new BigDecimal("200.00")
        );

        when(analyticsService.getDailyTrends(1L))
                .thenReturn(List.of(trend));

        mockMvc.perform(
                get("/api/analytics/trends")
                        .principal(
                                new UsernamePasswordAuthenticationToken(
                                        "1",
                                        null,
                                        List.of()
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].date").value("2026-09-12"))
        .andExpect(jsonPath("$[0].inflow").value(500.00))
        .andExpect(jsonPath("$[0].outflow").value(200.00));
    }
}