package com.confiance.portfolio.dto;

import com.confiance.common.enums.Market;
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
public class UserHoldingResponse {

    private Long id;
    private Long userId;
    private Market market;
    private String symbol;
    private String companyName;
    private String currency;
    private BigDecimal quantity;
    private BigDecimal averageBuyPrice;
    private LocalDate boughtOn;
    private BigDecimal investedAmount;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPL;
    private BigDecimal unrealizedPLPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
