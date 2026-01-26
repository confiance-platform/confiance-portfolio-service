package com.confiance.portfolio.controller;

import com.confiance.common.dto.ApiResponse;
import com.confiance.common.dto.PageResponse;
import com.confiance.common.enums.Market;
import com.confiance.common.enums.TradeStatus;
import com.confiance.portfolio.dto.SellTradeRequest;
import com.confiance.portfolio.dto.TradeRequest;
import com.confiance.portfolio.dto.TradeResponse;
import com.confiance.portfolio.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Tag(name = "Trades", description = "Trade Recording and Management APIs")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create Trade", description = "Record a new buy trade")
    public ResponseEntity<ApiResponse<TradeResponse>> createTrade(
            @PathVariable Long userId,
            @Valid @RequestBody TradeRequest request) {
        TradeResponse response = tradeService.createTrade(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trade recorded successfully", response));
    }

    @PutMapping("/{tradeId}/user/{userId}")
    @Operation(summary = "Update Trade", description = "Update an existing trade")
    public ResponseEntity<ApiResponse<TradeResponse>> updateTrade(
            @PathVariable Long tradeId,
            @PathVariable Long userId,
            @Valid @RequestBody TradeRequest request) {
        TradeResponse response = tradeService.updateTrade(userId, tradeId, request);
        return ResponseEntity.ok(ApiResponse.success("Trade updated successfully", response));
    }

    @PostMapping("/{tradeId}/user/{userId}/sell")
    @Operation(summary = "Record Sell", description = "Record a sell against an existing buy trade")
    public ResponseEntity<ApiResponse<TradeResponse>> recordSell(
            @PathVariable Long tradeId,
            @PathVariable Long userId,
            @Valid @RequestBody SellTradeRequest request) {
        TradeResponse response = tradeService.recordSell(userId, tradeId, request);
        return ResponseEntity.ok(ApiResponse.success("Sell recorded successfully", response));
    }

    @GetMapping("/{tradeId}/user/{userId}")
    @Operation(summary = "Get Trade", description = "Get trade by ID")
    public ResponseEntity<ApiResponse<TradeResponse>> getTrade(
            @PathVariable Long tradeId,
            @PathVariable Long userId) {
        TradeResponse response = tradeService.getTradeById(userId, tradeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User Trades", description = "Get all trades for a user")
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getUserTrades(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "buyDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        PageResponse<TradeResponse> response = tradeService.getUserTrades(userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/filter")
    @Operation(summary = "Get Trades with Filters", description = "Get trades filtered by market and status")
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getTradesWithFilters(
            @PathVariable Long userId,
            @RequestParam(required = false) Market market,
            @RequestParam(required = false) TradeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TradeResponse> response = tradeService.getUserTradesWithFilters(userId, market, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get Trades by Status", description = "Get trades by status (OPEN, CLOSED, PARTIALLY_SOLD)")
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getTradesByStatus(
            @PathVariable Long userId,
            @PathVariable TradeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TradeResponse> response = tradeService.getUserTradesByStatus(userId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/date-range")
    @Operation(summary = "Get Trades by Date Range", description = "Get trades within a date range")
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getTradesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TradeResponse> response = tradeService.getUserTradesByDateRange(userId, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get P&L Summary", description = "Get profit/loss summary for a user")
    public ResponseEntity<ApiResponse<TradeService.UserPLSummary>> getUserPLSummary(@PathVariable Long userId) {
        TradeService.UserPLSummary summary = tradeService.getUserPLSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @Operation(summary = "Get All Trades (Admin)", description = "Get all trades across all users")
    public ResponseEntity<ApiResponse<PageResponse<TradeResponse>>> getAllTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TradeResponse> response = tradeService.getAllTrades(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{tradeId}/user/{userId}")
    @Operation(summary = "Delete Trade", description = "Delete a trade")
    public ResponseEntity<ApiResponse<Void>> deleteTrade(
            @PathVariable Long tradeId,
            @PathVariable Long userId) {
        tradeService.deleteTrade(userId, tradeId);
        return ResponseEntity.ok(ApiResponse.success("Trade deleted successfully", null));
    }
}
