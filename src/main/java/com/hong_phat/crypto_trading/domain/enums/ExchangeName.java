package com.hong_phat.crypto_trading.domain.enums;

import lombok.Getter;

/**
 * The enum Exchange name.
 */
@Getter
public enum ExchangeName {
    /**
     * Binance exchange name.
     */
    BINANCE("Binance"),
    /**
     * Huobi exchange name.
     */
    HUOBI("Huobi");

    private final String displayName;

    ExchangeName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
