package com.confiance.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalInvested = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalReturns = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal returnsPercentage = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}