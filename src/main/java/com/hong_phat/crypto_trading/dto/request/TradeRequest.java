package com.hong_phat.crypto_trading.dto.request;

import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeRequest implements Serializable {

    @NotNull
    @Schema(description = "ID of the user placing the trade", example = "1")
    private Long userId;

    @NotNull
    @Schema(description = "Trading pair for the trade", example = "ETHUSDT")
    private TradingPair tradingPair;

    @NotNull
    @Schema(description = "Type of the trade", example = "BUY")
    private TradeType tradeType;

    @NotNull
    @Schema(description = "Quantity of the asset to trade", example = "0.5")
    private BigDecimal quantity;

    @NotBlank
    @Schema(description = "Username of the user placing the trade", example = "hong_phat")
    private String username;
}
