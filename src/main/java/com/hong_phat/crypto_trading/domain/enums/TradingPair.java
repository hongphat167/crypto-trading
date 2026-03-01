package com.hong_phat.crypto_trading.domain.enums;

import lombok.Getter;

/**
 * The enum Trading pair.
 */
@Getter
public enum TradingPair {
    /**
     * Ethusdt trading pair.
     */
    ETHUSDT("Ethereum"),
    /**
     * Btcusdt trading pair.
     */
    BTCUSDT("Bitcoin");

    private final String name;

    TradingPair(String name) {
        this.name = name;
    }
}
