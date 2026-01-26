package com.confiance.portfolio.service;

import com.confiance.common.dto.PageResponse;
import com.confiance.common.enums.Market;
import com.confiance.common.exception.ResourceNotFoundException;
import com.confiance.portfolio.dto.HoldingSummary;
import com.confiance.portfolio.dto.UserHoldingResponse;
import com.confiance.portfolio.entity.UserHolding;
import com.confiance.portfolio.repository.UserHoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserHoldingService {

    private final UserHoldingRepository holdingRepository;

    @Transactional
    public UserHoldingResponse addOrUpdateHolding(Long userId, Market market, String symbol,
                                                   BigDecimal quantity, BigDecimal price,
                                                   String companyName, String currency) {
        log.info("Adding/updating holding for user: {} symbol: {}", userId, symbol);

        Optional<UserHolding> existingHolding = holdingRepository.findByUserIdAndSymbolAndMarket(userId, symbol.toUpperCase(), market);

        UserHolding holding;
        if (existingHolding.isPresent()) {
            holding = existingHolding.get();
            holding.addShares(quantity, price);
        } else {
            holding = UserHolding.builder()
                    .userId(userId)
                    .market(market)
                    .symbol(symbol.toUpperCase())
                    .companyName(companyName)
                    .currency(currency != null ? currency : market.getDefaultCurrency())
                    .quantity(quantity)
                    .averageBuyPrice(price)
                    .boughtOn(LocalDate.now())
                    .build();
        }

        UserHolding saved = holdingRepository.save(holding);
        return toResponse(saved);
    }

    @Transactional
    public UserHoldingResponse reduceHolding(Long userId, Market market, String symbol, BigDecimal quantity) {
        UserHolding holding = holdingRepository.findByUserIdAndSymbolAndMarket(userId, symbol.toUpperCase(), market)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", "symbol", symbol));

        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Cannot sell more than held quantity");
        }

        holding.removeShares(quantity);
        UserHolding saved = holdingRepository.save(holding);
        return toResponse(saved);
    }

    @Transactional
    public void updateCurrentPrice(Long userId, Market market, String symbol, BigDecimal currentPrice) {
        holdingRepository.findByUserIdAndSymbolAndMarket(userId, symbol.toUpperCase(), market)
                .ifPresent(holding -> {
                    holding.setCurrentPrice(currentPrice);
                    holdingRepository.save(holding);
                });
    }

    public List<UserHoldingResponse> getUserHoldings(Long userId) {
        return holdingRepository.findActiveHoldingsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<UserHoldingResponse> getUserHoldingsPaged(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("investedAmount").descending());
        Page<UserHolding> holdingPage = holdingRepository.findByUserId(userId, pageable);
        return buildPageResponse(holdingPage);
    }

    public List<UserHoldingResponse> getUserHoldingsByMarket(Long userId, Market market) {
        return holdingRepository.findByUserIdAndMarket(userId, market)
                .stream()
                .filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(this::toResponse)
                .toList();
    }

    public HoldingSummary getUserHoldingSummary(Long userId) {
        List<UserHolding> holdings = holdingRepository.findActiveHoldingsByUser(userId);

        BigDecimal totalInvested = holdingRepository.getTotalInvestedByUser(userId);
        BigDecimal totalCurrentValue = holdingRepository.getTotalCurrentValueByUser(userId);
        BigDecimal totalUnrealizedPL = holdingRepository.getTotalUnrealizedPLByUser(userId);

        totalInvested = totalInvested != null ? totalInvested : BigDecimal.ZERO;
        totalCurrentValue = totalCurrentValue != null ? totalCurrentValue : BigDecimal.ZERO;
        totalUnrealizedPL = totalUnrealizedPL != null ? totalUnrealizedPL : BigDecimal.ZERO;

        BigDecimal totalUnrealizedPLPercentage = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalUnrealizedPLPercentage = totalUnrealizedPL.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return HoldingSummary.builder()
                .userId(userId)
                .totalInvestedAmount(totalInvested)
                .totalCurrentValue(totalCurrentValue)
                .totalUnrealizedPL(totalUnrealizedPL)
                .totalUnrealizedPLPercentage(totalUnrealizedPLPercentage)
                .totalHoldings(holdings.size())
                .holdings(holdings.stream().map(this::toResponse).toList())
                .build();
    }

    public UserHoldingResponse getHoldingBySymbol(Long userId, Market market, String symbol) {
        return holdingRepository.findByUserIdAndSymbolAndMarket(userId, symbol.toUpperCase(), market)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", "symbol", symbol));
    }

    // Admin methods
    public List<UserHoldingResponse> getAllHoldingsBySymbol(String symbol) {
        return holdingRepository.findAllHoldingsBySymbol(symbol.toUpperCase())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<Long> getAllUsersWithHoldings() {
        return holdingRepository.findAllUsersWithHoldings();
    }

    private UserHoldingResponse toResponse(UserHolding h) {
        return UserHoldingResponse.builder()
                .id(h.getId())
                .userId(h.getUserId())
                .market(h.getMarket())
                .symbol(h.getSymbol())
                .companyName(h.getCompanyName())
                .currency(h.getCurrency())
                .quantity(h.getQuantity())
                .averageBuyPrice(h.getAverageBuyPrice())
                .boughtOn(h.getBoughtOn())
                .investedAmount(h.getInvestedAmount())
                .currentPrice(h.getCurrentPrice())
                .currentValue(h.getCurrentValue())
                .unrealizedPL(h.getUnrealizedPL())
                .unrealizedPLPercentage(h.getUnrealizedPLPercentage())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }

    private PageResponse<UserHoldingResponse> buildPageResponse(Page<UserHolding> page) {
        return PageResponse.<UserHoldingResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }
}
