package com.confiance.portfolio.entity;

import com.confiance.common.enums.Market;
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

@Entity
@Table(name = "user_holdings", indexes = {
        @Index(name = "idx_holding_user_id", columnList = "userId"),
        @Index(name = "idx_holding_symbol", columnList = "symbol"),
        @Index(name = "idx_holding_market", columnList = "market")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_symbol_market", columnNames = {"userId", "symbol", "market"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserHolding {

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

    // Total quantity held
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    // Average buy price (weighted average)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal averageBuyPrice;

    // First buy date
    private LocalDate boughtOn;

    // Total invested amount (quantity * averageBuyPrice)
    @Column(precision = 19, scale = 2)
    private BigDecimal investedAmount;

    // Current market price (to be updated)
    @Column(precision = 19, scale = 2)
    private BigDecimal currentPrice;

    // Current value (quantity * currentPrice)
    @Column(precision = 19, scale = 2)
    private BigDecimal currentValue;

    // Unrealized P&L
    @Column(precision = 19, scale = 2)
    private BigDecimal unrealizedPL;

    // Unrealized P&L percentage
    @Column(precision = 10, scale = 2)
    private BigDecimal unrealizedPLPercentage;

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
        if (quantity != null && averageBuyPrice != null) {
            this.investedAmount = quantity.multiply(averageBuyPrice).setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate current value and unrealized P&L if current price is available
        if (currentPrice != null && quantity != null) {
            this.currentValue = quantity.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);

            if (investedAmount != null && investedAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.unrealizedPL = currentValue.subtract(investedAmount).setScale(2, RoundingMode.HALF_UP);
                this.unrealizedPLPercentage = unrealizedPL.divide(investedAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    // Helper method to update average price when adding more shares
    public void addShares(BigDecimal newQuantity, BigDecimal newPrice) {
        BigDecimal totalOldValue = this.quantity.multiply(this.averageBuyPrice);
        BigDecimal totalNewValue = newQuantity.multiply(newPrice);
        BigDecimal totalQuantity = this.quantity.add(newQuantity);

        this.averageBuyPrice = totalOldValue.add(totalNewValue)
                .divide(totalQuantity, 2, RoundingMode.HALF_UP);
        this.quantity = totalQuantity;
    }

    // Helper method to remove shares
    public void removeShares(BigDecimal sellQuantity) {
        this.quantity = this.quantity.subtract(sellQuantity);
    }
}
