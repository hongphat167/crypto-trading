package com.hong_phat.crypto_trading.cqrs.command;

import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The type Execute trade command.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteTradeCommand implements Serializable {

    private Long userId;
    private String username;
    private TradingPair tradingPair;
    private TradeType tradeType;
    private BigDecimal quantity;
}
