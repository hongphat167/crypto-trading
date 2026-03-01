package com.hong_phat.crypto_trading.controller;

import com.hong_phat.crypto_trading.cqrs.query.handler.GetBestPriceQueryHandler;
import com.hong_phat.crypto_trading.dto.response.AggregatedPriceResponse;
import com.hong_phat.crypto_trading.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The type Price controller.
 */
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Tag(name = "Price", description = "APIs for retrieving aggregated crypto prices")
public class PriceController {

    private final GetBestPriceQueryHandler getBestPriceQueryHandler;

    /**
     * Gets latest best prices.
     *
     * @return the latest best prices
     */
    @GetMapping
    @Operation(summary = "Get latest best aggregated prices", description = "Returns the latest best bid and ask prices")
    public ResponseEntity<@NonNull BaseResponse<List<AggregatedPriceResponse>>> getLatestBestPrices() {

        List<AggregatedPriceResponse> aggregatedPriceResponses = getBestPriceQueryHandler.handle();

        return ResponseEntity.ok(BaseResponse.success(aggregatedPriceResponses));
    }
}
