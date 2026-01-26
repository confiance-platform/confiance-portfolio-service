package com.confiance.portfolio.dto;

import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {

    private Long id;
    private Long userId;
    private Market market;
    private String symbol;
    private String companyName;
    private String currency;

    // Buy details
    private LocalDate buyDate;
    private BigDecimal buyPrice;
    private BigDecimal buyQuantity;

    // Sell details
    private LocalDate sellDate;
    private BigDecimal sellPrice;
    private BigDecimal sellQuantity;

    // Calculated fields
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private Integer positionHeldDays;
    private BigDecimal remainingQuantity;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;

    private TradeStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
