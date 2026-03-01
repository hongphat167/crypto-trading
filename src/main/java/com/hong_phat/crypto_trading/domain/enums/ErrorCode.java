package com.hong_phat.crypto_trading.domain.enums;

import lombok.Getter;

/**
 * The enum Error code.
 */
@Getter
public enum ErrorCode {
    /**
     * The Insufficient balance.
     */
    INSUFFICIENT_BALANCE("01", "Insufficient balance"),
    /**
     * The Invalid quantity.
     */
    INVALID_QUANTITY("02", "Invalid quantity"),
    /**
     * The Invalid trade type.
     */
    INVALID_TRADE_TYPE("03", "Invalid trade type"),
    /**
     * The Invalid trading pair.
     */
    INVALID_TRADING_PAIR("04", "Invalid trading pair"),
    /**
     * The User not found.
     */
    USER_NOT_FOUND("04", "User not found"),
    /**
     * The Price not available.
     */
    PRICE_NOT_AVAILABLE("05", "Price not available"),
    /**
     * The Insufficient usdt balance.
     */
    INSUFFICIENT_USDT_BALANCE("06", "Insufficient USDT balance"),
    /**
     * The Insufficient crypto balance.
     */
    INSUFFICIENT_CRYPTO_BALANCE("07", "Insufficient crypto balance");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
