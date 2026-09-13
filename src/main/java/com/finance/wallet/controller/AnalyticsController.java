package com.finance.wallet.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.wallet.dto.AnalyticsResponse;
import com.finance.wallet.dto.AnalyticsTrendResponse;
import com.finance.wallet.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsResponse getAnalytics(
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        return analyticsService.getAnalytics(userId);
    }

    @GetMapping("/trends")
    public List<AnalyticsTrendResponse> getDailyTrends(
            Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        return analyticsService.getDailyTrends(userId);
    }
}