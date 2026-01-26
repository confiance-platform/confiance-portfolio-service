package com.confiance.portfolio.entity;

import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trade_user_id", columnList = "userId"),
        @Index(name = "idx_trade_symbol", columnList = "symbol"),
        @Index(name = "idx_trade_market", columnList = "market"),
        @Index(name = "idx_trade_status", columnList = "status"),
        @Index(name = "idx_trade_buy_date", columnList = "buyDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Market market;

    @Column(nullable = false, length = 20)
    private String symbol;

    private String companyName;

    @Column(length = 10)
    private String currency;

    // Buy details
    @Column(nullable = false)
    private LocalDate buyDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal buyQuantity;

    // Sell details (optional - filled when position is closed)
    private LocalDate sellDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal sellPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal sellQuantity;

    // Calculated fields
    @Column(precision = 19, scale = 2)
    private BigDecimal profitLoss;

    @Column(precision = 10, scale = 2)
    private BigDecimal profitLossPercentage;

    private Integer positionHeldDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TradeStatus status = TradeStatus.OPEN;

    // Remaining quantity (for partial sells)
    @Column(precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    // Total investment value (buyPrice * buyQuantity)
    @Column(precision = 19, scale = 2)
    private BigDecimal investedAmount;

    // Current value or sold value
    @Column(precision = 19, scale = 2)
    private BigDecimal currentValue;

    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateFields() {
        // Calculate invested amount
        if (buyPrice != null && buyQuantity != null) {
            this.investedAmount = buyPrice.multiply(buyQuantity).setScale(2, RoundingMode.HALF_UP);
        }

        // Initialize remaining quantity on first save
        if (remainingQuantity == null && buyQuantity != null) {
            this.remainingQuantity = buyQuantity;
        }

        // Calculate profit/loss if sell data exists
        if (sellPrice != null && sellQuantity != null && buyPrice != null) {
            BigDecimal soldValue = sellPrice.multiply(sellQuantity);
            BigDecimal costBasis = buyPrice.multiply(sellQuantity);
            this.profitLoss = soldValue.subtract(costBasis).setScale(2, RoundingMode.HALF_UP);

            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                this.profitLossPercentage = profitLoss.divide(costBasis, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Calculate position held days
        if (buyDate != null) {
            LocalDate endDate = sellDate != null ? sellDate : LocalDate.now();
            this.positionHeldDays = (int) ChronoUnit.DAYS.between(buyDate, endDate);
        }

        // Update current value
        if (sellPrice != null && sellQuantity != null) {
            this.currentValue = sellPrice.multiply(sellQuantity).setScale(2, RoundingMode.HALF_UP);
        }

        // Update status based on quantities
        if (remainingQuantity != null && buyQuantity != null) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                this.status = TradeStatus.CLOSED;
            } else if (remainingQuantity.compareTo(buyQuantity) < 0) {
                this.status = TradeStatus.PARTIALLY_SOLD;
            }
        }
    }
}
