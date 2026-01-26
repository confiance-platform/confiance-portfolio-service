package com.confiance.portfolio.dto;

import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import jakarta.validation.constraints.NotBlank;
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
public class TradeRequest {

    @NotNull(message = "Market is required")
    private Market market;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    private String companyName;

    private String currency;

    @NotNull(message = "Buy date is required")
    private LocalDate buyDate;

    @NotNull(message = "Buy price is required")
    @Positive(message = "Buy price must be positive")
    private BigDecimal buyPrice;

    @NotNull(message = "Buy quantity is required")
    @Positive(message = "Buy quantity must be positive")
    private BigDecimal buyQuantity;

    // Optional sell details
    private LocalDate sellDate;

    @Positive(message = "Sell price must be positive")
    private BigDecimal sellPrice;

    @Positive(message = "Sell quantity must be positive")
    private BigDecimal sellQuantity;

    private TradeStatus status;

    private String notes;
}
