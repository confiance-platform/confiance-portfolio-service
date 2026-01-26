package com.confiance.portfolio.controller;

import com.confiance.common.dto.ApiResponse;
import com.confiance.common.dto.PageResponse;
import com.confiance.common.enums.Market;
import com.confiance.portfolio.dto.HoldingSummary;
import com.confiance.portfolio.dto.UserHoldingResponse;
import com.confiance.portfolio.service.UserHoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
@RequiredArgsConstructor
@Tag(name = "Holdings", description = "User Holdings Management APIs")
public class UserHoldingController {

    private final UserHoldingService holdingService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get User Holdings", description = "Get all holdings for a user")
    public ResponseEntity<ApiResponse<List<UserHoldingResponse>>> getUserHoldings(@PathVariable Long userId) {
        List<UserHoldingResponse> response = holdingService.getUserHoldings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/paged")
    @Operation(summary = "Get User Holdings (Paged)", description = "Get paginated holdings for a user")
    public ResponseEntity<ApiResponse<PageResponse<UserHoldingResponse>>> getUserHoldingsPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserHoldingResponse> response = holdingService.getUserHoldingsPaged(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/market/{market}")
    @Operation(summary = "Get Holdings by Market", description = "Get holdings for a user filtered by market")
    public ResponseEntity<ApiResponse<List<UserHoldingResponse>>> getUserHoldingsByMarket(
            @PathVariable Long userId,
            @PathVariable Market market) {
        List<UserHoldingResponse> response = holdingService.getUserHoldingsByMarket(userId, market);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Get Holdings Summary", description = "Get holding summary with total values for a user")
    public ResponseEntity<ApiResponse<HoldingSummary>> getUserHoldingSummary(@PathVariable Long userId) {
        HoldingSummary response = holdingService.getUserHoldingSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/symbol/{symbol}")
    @Operation(summary = "Get Holding by Symbol", description = "Get specific holding by symbol")
    public ResponseEntity<ApiResponse<UserHoldingResponse>> getHoldingBySymbol(
            @PathVariable Long userId,
            @PathVariable String symbol,
            @RequestParam Market market) {
        UserHoldingResponse response = holdingService.getHoldingBySymbol(userId, market, symbol);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Admin endpoints
    @GetMapping("/admin/symbol/{symbol}")
    @Operation(summary = "Get All Holdings by Symbol (Admin)", description = "Get all users' holdings for a specific symbol")
    public ResponseEntity<ApiResponse<List<UserHoldingResponse>>> getAllHoldingsBySymbol(@PathVariable String symbol) {
        List<UserHoldingResponse> response = holdingService.getAllHoldingsBySymbol(symbol);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admin/users-with-holdings")
    @Operation(summary = "Get Users with Holdings (Admin)", description = "Get list of all user IDs that have holdings")
    public ResponseEntity<ApiResponse<List<Long>>> getUsersWithHoldings() {
        List<Long> response = holdingService.getAllUsersWithHoldings();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
