package com.confiance.portfolio.repository;

import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import com.confiance.portfolio.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByUserId(Long userId, Pageable pageable);

    Page<Trade> findByUserIdAndStatus(Long userId, TradeStatus status, Pageable pageable);

    Page<Trade> findByUserIdAndMarket(Long userId, Market market, Pageable pageable);

    List<Trade> findByUserIdAndSymbol(Long userId, String symbol);

    List<Trade> findByUserIdAndStatus(Long userId, TradeStatus status);

    @Query("SELECT t FROM Trade t WHERE t.userId = :userId AND t.buyDate BETWEEN :startDate AND :endDate ORDER BY t.buyDate DESC")
    Page<Trade> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);

    @Query("SELECT SUM(t.profitLoss) FROM Trade t WHERE t.userId = :userId AND t.status = 'CLOSED'")
    BigDecimal getTotalProfitLossForUser(@Param("userId") Long userId);

    @Query("SELECT SUM(t.investedAmount) FROM Trade t WHERE t.userId = :userId AND t.status IN ('OPEN', 'PARTIALLY_SOLD')")
    BigDecimal getTotalInvestedAmountForUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Trade t WHERE t.userId = :userId ORDER BY t.buyDate DESC")
    List<Trade> findRecentTradesByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Trade t WHERE t.userId = :userId AND " +
           "(:market IS NULL OR t.market = :market) AND " +
           "(:status IS NULL OR t.status = :status) " +
           "ORDER BY t.buyDate DESC")
    Page<Trade> findWithFilters(@Param("userId") Long userId,
                                 @Param("market") Market market,
                                 @Param("status") TradeStatus status,
                                 Pageable pageable);

    // For admin to see all users' trades
    @Query("SELECT t FROM Trade t ORDER BY t.createdAt DESC")
    Page<Trade> findAllTrades(Pageable pageable);

    // Get open positions for a specific symbol across all users (for admin reporting)
    @Query("SELECT t FROM Trade t WHERE t.symbol = :symbol AND t.status IN ('OPEN', 'PARTIALLY_SOLD')")
    List<Trade> findOpenPositionsBySymbol(@Param("symbol") String symbol);
}
