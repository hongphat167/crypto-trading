package com.hong_phat.crypto_trading.controller;

import com.hong_phat.crypto_trading.cqrs.query.GetWalletBalanceQuery;
import com.hong_phat.crypto_trading.cqrs.query.handler.GetWalletBalanceQueryHandler;
import com.hong_phat.crypto_trading.dto.response.BaseResponse;
import com.hong_phat.crypto_trading.dto.response.WalletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Wallet controller.
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "APIs for retrieving wallet balances")
public class WalletController {

    private final GetWalletBalanceQueryHandler getWalletBalanceQueryHandler;

    /**
     * Gets wallet balance.
     *
     * @param userId the user id
     * @return the wallet balance
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get wallet balance", description = "Returns all crypto currency wallet balances for a given user")
    public ResponseEntity<@NonNull BaseResponse<WalletResponse>> getWalletBalance(@PathVariable Long userId) {

        WalletResponse walletResponses = getWalletBalanceQueryHandler.handle(
                GetWalletBalanceQuery.builder()
                        .userId(userId)
                        .build()
        );

        return ResponseEntity.ok(BaseResponse.success(walletResponses));
    }
}
