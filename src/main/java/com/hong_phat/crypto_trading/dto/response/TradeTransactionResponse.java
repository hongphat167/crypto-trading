package com.hong_phat.crypto_trading.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hong_phat.crypto_trading.domain.enums.TradeTransactionStatus;
import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Trade response dto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeTransactionResponse implements Serializable {

    @Schema(description = "ID of the trade", example = "1")
    private Long id;

    @Schema(description = "ID of the user who placed the trade", example = "123")
    private Long userId;

    @Schema(description = "Username of the user who placed the trade", example = "hongphat")
    private TradingPair tradingPair;

    @Schema(description = "Type of the trade", example = "BUY")
    private TradeType tradeType;

    @Schema(description = "Price at which the trade was executed", example = "30000.00")
    private BigDecimal price;

    @Schema(description = "Quantity of the asset traded", example = "0.5")
    private BigDecimal quantity;

    @Schema(description = "Total amount of the trade (price * quantity)", example = "15000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Status of the trade transaction", example = "SUCCESS")
    private TradeTransactionStatus status;

    @Schema(description = "Date and time when the trade was created", example = "01-03-2026 14:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;
}
