package com.hong_phat.crypto_trading.constants;

import java.util.List;

/**
 * The type Crypto trading constants.
 */
public class CryptoTradingConstants {
    /**
     * The constant BINANCE_URL.
     */
    public static final String BINANCE_URL = "https://api.binance.com/api/v3/ticker/bookTicker";
    /**
     * The constant HUOBI_URL.
     */
    public static final String HUOBI_URL = "https://api.huobi.pro/market/tickers";
    /**
     * The constant HUOBI_SUPPORTED_PAIRS.
     */
    public static final List<String> SUPPORTED_PAIRS = List.of("ethusdt", "btcusdt");
    /**
     * The constant SUCCESS_CODE.
     */
    public static final String SUCCESS_CODE = "00";
    /**
     * The constant SUCCESS_MESSAGE.
     */
    public static final String SUCCESS_MESSAGE = "Success";

    private CryptoTradingConstants() {
        // Private constructor to prevent instantiation
    }
}
