package com.confiance.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellTradeRequest {

    @NotNull(message = "Sell date is required")
    private LocalDate sellDate;

    @NotNull(message = "Sell price is required")
    @Positive(message = "Sell price must be positive")
    private BigDecimal sellPrice;

    @NotNull(message = "Sell quantity is required")
    @Positive(message = "Sell quantity must be positive")
    private BigDecimal sellQuantity;

    private String notes;
}
