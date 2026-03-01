package com.hong_phat.crypto_trading.domain.enums;

import lombok.Getter;

/**
 * The enum Trade transaction status.
 */
@Getter
public enum TradeTransactionStatus {
    /**
     * Pending trade transaction status.
     */
    PENDING,
    /**
     * Success trade transaction status.
     */
    SUCCESS,
    /**
     * Failed trade transaction status.
     */
    FAILED
}
