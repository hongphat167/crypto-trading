package com.hong_phat.crypto_trading.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Aggregated price response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedPriceResponse implements Serializable {

    @Schema(description = "Trading pair for the price", example = "ETHUSDT")
    private String tradingPair;

    @Schema(description = "Best bid price for the trading pair", example = "2000.50")
    private BigDecimal bidPrice;

    @Schema(description = "Best ask price for the trading pair", example = "2001.00")
    private BigDecimal askPrice;

    @Schema(description = "Timestamp when the price was aggregated", example = "01-03-2026 15:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;
}
