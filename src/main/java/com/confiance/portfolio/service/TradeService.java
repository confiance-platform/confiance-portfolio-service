package com.confiance.portfolio.service;

import com.confiance.common.dto.PageResponse;
import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import com.confiance.common.exception.BadRequestException;
import com.confiance.common.exception.ResourceNotFoundException;
import com.confiance.portfolio.dto.SellTradeRequest;
import com.confiance.portfolio.dto.TradeRequest;
import com.confiance.portfolio.dto.TradeResponse;
import com.confiance.portfolio.entity.Trade;
import com.confiance.portfolio.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    @Transactional
    public TradeResponse createTrade(Long userId, TradeRequest request) {
        log.info("Creating trade for user: {} symbol: {}", userId, request.getSymbol());

        Trade trade = Trade.builder()
                .userId(userId)
                .market(request.getMarket())
                .symbol(request.getSymbol().toUpperCase())
                .companyName(request.getCompanyName())
                .currency(request.getCurrency() != null ? request.getCurrency() :
                        request.getMarket().getDefaultCurrency())
                .buyDate(request.getBuyDate())
                .buyPrice(request.getBuyPrice())
                .buyQuantity(request.getBuyQuantity())
                .sellDate(request.getSellDate())
                .sellPrice(request.getSellPrice())
                .sellQuantity(request.getSellQuantity())
                .status(request.getStatus() != null ? request.getStatus() : TradeStatus.OPEN)
                .notes(request.getNotes())
                .build();

        Trade saved = tradeRepository.save(trade);
        return toResponse(saved);
    }

    @Transactional
    public TradeResponse updateTrade(Long userId, Long tradeId, TradeRequest request) {
        Trade trade = findByIdAndUserId(tradeId, userId);

        if (request.getMarket() != null) trade.setMarket(request.getMarket());
        if (request.getSymbol() != null) trade.setSymbol(request.getSymbol().toUpperCase());
        if (request.getCompanyName() != null) trade.setCompanyName(request.getCompanyName());
        if (request.getCurrency() != null) trade.setCurrency(request.getCurrency());
        if (request.getBuyDate() != null) trade.setBuyDate(request.getBuyDate());
        if (request.getBuyPrice() != null) trade.setBuyPrice(request.getBuyPrice());
        if (request.getBuyQuantity() != null) trade.setBuyQuantity(request.getBuyQuantity());
        if (request.getSellDate() != null) trade.setSellDate(request.getSellDate());
        if (request.getSellPrice() != null) trade.setSellPrice(request.getSellPrice());
        if (request.getSellQuantity() != null) trade.setSellQuantity(request.getSellQuantity());
        if (request.getStatus() != null) trade.setStatus(request.getStatus());
        if (request.getNotes() != null) trade.setNotes(request.getNotes());

        Trade saved = tradeRepository.save(trade);
        return toResponse(saved);
    }

    @Transactional
    public TradeResponse recordSell(Long userId, Long tradeId, SellTradeRequest request) {
        Trade trade = findByIdAndUserId(tradeId, userId);

        if (trade.getStatus() == TradeStatus.CLOSED) {
            throw new BadRequestException("Trade is already closed");
        }

        BigDecimal availableQuantity = trade.getRemainingQuantity() != null ?
                trade.getRemainingQuantity() : trade.getBuyQuantity();

        if (request.getSellQuantity().compareTo(availableQuantity) > 0) {
            throw new BadRequestException("Sell quantity exceeds available quantity. Available: " + availableQuantity);
        }

        trade.setSellDate(request.getSellDate());
        trade.setSellPrice(request.getSellPrice());
        trade.setSellQuantity(request.getSellQuantity());
        trade.setRemainingQuantity(availableQuantity.subtract(request.getSellQuantity()));

        if (request.getNotes() != null) {
            String existingNotes = trade.getNotes() != null ? trade.getNotes() + " | " : "";
            trade.setNotes(existingNotes + request.getNotes());
        }

        Trade saved = tradeRepository.save(trade);
        return toResponse(saved);
    }

    public TradeResponse getTradeById(Long userId, Long tradeId) {
        return toResponse(findByIdAndUserId(tradeId, userId));
    }

    public PageResponse<TradeResponse> getUserTrades(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Trade> tradePage = tradeRepository.findByUserId(userId, pageable);
        return buildPageResponse(tradePage);
    }

    public PageResponse<TradeResponse> getUserTradesWithFilters(Long userId, Market market, TradeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("buyDate").descending());
        Page<Trade> tradePage = tradeRepository.findWithFilters(userId, market, status, pageable);
        return buildPageResponse(tradePage);
    }

    public PageResponse<TradeResponse> getUserTradesByStatus(Long userId, TradeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("buyDate").descending());
        Page<Trade> tradePage = tradeRepository.findByUserIdAndStatus(userId, status, pageable);
        return buildPageResponse(tradePage);
    }

    public PageResponse<TradeResponse> getUserTradesByDateRange(Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Trade> tradePage = tradeRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return buildPageResponse(tradePage);
    }

    // Admin endpoints
    public PageResponse<TradeResponse> getAllTrades(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Trade> tradePage = tradeRepository.findAllTrades(pageable);
        return buildPageResponse(tradePage);
    }

    public UserPLSummary getUserPLSummary(Long userId) {
        BigDecimal totalPL = tradeRepository.getTotalProfitLossForUser(userId);
        BigDecimal totalInvested = tradeRepository.getTotalInvestedAmountForUser(userId);
        List<Trade> openTrades = tradeRepository.findByUserIdAndStatus(userId, TradeStatus.OPEN);
        List<Trade> closedTrades = tradeRepository.findByUserIdAndStatus(userId, TradeStatus.CLOSED);

        return UserPLSummary.builder()
                .userId(userId)
                .totalProfitLoss(totalPL != null ? totalPL : BigDecimal.ZERO)
                .totalInvestedAmount(totalInvested != null ? totalInvested : BigDecimal.ZERO)
                .openTradesCount(openTrades.size())
                .closedTradesCount(closedTrades.size())
                .build();
    }

    @Transactional
    public void deleteTrade(Long userId, Long tradeId) {
        Trade trade = findByIdAndUserId(tradeId, userId);
        tradeRepository.delete(trade);
    }

    private Trade findByIdAndUserId(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));

        if (!trade.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Trade", "id", tradeId);
        }

        return trade;
    }

    private TradeResponse toResponse(Trade t) {
        return TradeResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .market(t.getMarket())
                .symbol(t.getSymbol())
                .companyName(t.getCompanyName())
                .currency(t.getCurrency())
                .buyDate(t.getBuyDate())
                .buyPrice(t.getBuyPrice())
                .buyQuantity(t.getBuyQuantity())
                .sellDate(t.getSellDate())
                .sellPrice(t.getSellPrice())
                .sellQuantity(t.getSellQuantity())
                .profitLoss(t.getProfitLoss())
                .profitLossPercentage(t.getProfitLossPercentage())
                .positionHeldDays(t.getPositionHeldDays())
                .remainingQuantity(t.getRemainingQuantity())
                .investedAmount(t.getInvestedAmount())
                .currentValue(t.getCurrentValue())
                .status(t.getStatus())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private PageResponse<TradeResponse> buildPageResponse(Page<Trade> page) {
        return PageResponse.<TradeResponse>builder()
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

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserPLSummary {
        private Long userId;
        private BigDecimal totalProfitLoss;
        private BigDecimal totalInvestedAmount;
        private int openTradesCount;
        private int closedTradesCount;
    }
}
