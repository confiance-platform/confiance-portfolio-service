package com.confiance.portfolio.controller;

import com.confiance.common.dto.ApiResponse;
import com.confiance.portfolio.entity.Portfolio;
import com.confiance.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioRepository repository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Portfolio>> getUserPortfolio(@PathVariable Long userId) {
        return repository.findByUserId(userId)
                .map(portfolio -> ResponseEntity.ok(ApiResponse.success(portfolio)))
                .orElseGet(() -> {
                    Portfolio newPortfolio = Portfolio.builder().userId(userId).build();
                    Portfolio saved = repository.save(newPortfolio);
                    return ResponseEntity.ok(ApiResponse.success(saved));
                });
    }
}