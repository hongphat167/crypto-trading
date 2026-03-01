package com.hong_phat.crypto_trading.exception;

import lombok.Getter;

/**
 * The type Crypto trading exception.
 */
@Getter
public class CryptoTradingException extends RuntimeException {
    private final String code;

    /**
     * Instantiates a new Crypto trading exception.
     *
     * @param code    the code
     * @param message the message
     */
    public CryptoTradingException(String code, String message) {
        super(message);
        this.code = code;
    }
}
