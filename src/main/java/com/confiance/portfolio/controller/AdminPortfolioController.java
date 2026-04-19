package com.confiance.portfolio.controller;

import com.confiance.common.dto.ApiResponse;
import com.confiance.portfolio.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/portfolio")
@RequiredArgsConstructor
@Tag(name = "Admin Portfolio", description = "Admin Portfolio Statistics APIs")
public class AdminPortfolioController {

    private final TradeService tradeService;

    @GetMapping("/stats")
    @Operation(summary = "Get Portfolio Stats", description = "Get aggregated portfolio statistics for admin dashboard")
    public ResponseEntity<ApiResponse<TradeService.PortfolioStats>> getPortfolioStats() {
        TradeService.PortfolioStats stats = tradeService.getPortfolioStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
