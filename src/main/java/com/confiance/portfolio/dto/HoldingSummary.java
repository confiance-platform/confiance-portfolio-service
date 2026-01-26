package com.confiance.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingSummary {
    private Long userId;
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedPL;
    private BigDecimal totalUnrealizedPLPercentage;
    private int totalHoldings;
    private List<UserHoldingResponse> holdings;
}
