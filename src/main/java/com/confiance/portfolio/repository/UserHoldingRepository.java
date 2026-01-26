package com.confiance.portfolio.repository;

import com.confiance.common.enums.Market;
import com.confiance.portfolio.entity.UserHolding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserHoldingRepository extends JpaRepository<UserHolding, Long> {

    List<UserHolding> findByUserId(Long userId);

    Page<UserHolding> findByUserId(Long userId, Pageable pageable);

    List<UserHolding> findByUserIdAndMarket(Long userId, Market market);

    Optional<UserHolding> findByUserIdAndSymbolAndMarket(Long userId, String symbol, Market market);

    @Query("SELECT h FROM UserHolding h WHERE h.userId = :userId AND h.quantity > 0 ORDER BY h.investedAmount DESC")
    List<UserHolding> findActiveHoldingsByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(h.investedAmount) FROM UserHolding h WHERE h.userId = :userId AND h.quantity > 0")
    BigDecimal getTotalInvestedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(h.currentValue) FROM UserHolding h WHERE h.userId = :userId AND h.quantity > 0")
    BigDecimal getTotalCurrentValueByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(h.unrealizedPL) FROM UserHolding h WHERE h.userId = :userId AND h.quantity > 0")
    BigDecimal getTotalUnrealizedPLByUser(@Param("userId") Long userId);

    // For admin - get all holdings for a specific symbol
    @Query("SELECT h FROM UserHolding h WHERE h.symbol = :symbol AND h.quantity > 0")
    List<UserHolding> findAllHoldingsBySymbol(@Param("symbol") String symbol);

    // For admin - get all users with holdings
    @Query("SELECT DISTINCT h.userId FROM UserHolding h WHERE h.quantity > 0")
    List<Long> findAllUsersWithHoldings();

    @Query("SELECT h FROM UserHolding h WHERE h.userId = :userId AND " +
           "(:market IS NULL OR h.market = :market) AND h.quantity > 0 " +
           "ORDER BY h.investedAmount DESC")
    Page<UserHolding> findWithFilters(@Param("userId") Long userId,
                                       @Param("market") Market market,
                                       Pageable pageable);
}
