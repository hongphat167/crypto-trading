package com.hong_phat.crypto_trading.controller;

import com.hong_phat.crypto_trading.cqrs.command.ExecuteTradeCommand;
import com.hong_phat.crypto_trading.cqrs.command.handler.ExecuteTradeCommandHandler;
import com.hong_phat.crypto_trading.dto.request.TradeRequest;
import com.hong_phat.crypto_trading.dto.response.BaseResponse;
import com.hong_phat.crypto_trading.dto.response.TradeTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Trade controller.
 */
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@Tag(name = "Trade", description = "APIs for executing trades and viewing trade history")
public class TradeController {

    private final ExecuteTradeCommandHandler executeTradeCommandHandler;

    /**
     * Execute trade response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @Operation(summary = "Execute a trade", description = "Buy or sell crypto based on the latest best aggregated price")
    public ResponseEntity<@NonNull BaseResponse<TradeTransactionResponse>> executeTrade(@RequestBody TradeRequest request) {

        TradeTransactionResponse tradeTransactionResponse = executeTradeCommandHandler.handle(
                ExecuteTradeCommand.builder()
                        .userId(request.getUserId())
                        .tradingPair(request.getTradingPair())
                        .tradeType(request.getTradeType())
                        .quantity(request.getQuantity())
                        .build()
        );

        return ResponseEntity.ok(BaseResponse.success(tradeTransactionResponse));
    }
}
