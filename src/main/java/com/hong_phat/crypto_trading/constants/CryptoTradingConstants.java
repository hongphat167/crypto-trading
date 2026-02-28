package com.hong_phat.crypto_trading.constants;

import java.util.List;

/**
 * The type Crypto trading constants.
 */
public class CryptoTradingConstants {

    public static final String BINANCE_URL = "https://api.binance.com/api/v3/ticker/bookTicker";
    public static final String HUOBI_URL = "https://api.huobi.pro/market/tickers";

    public static final List<String> BINANCE_SUPPORTED_PAIRS = List.of("ETHUSDT", "BTCUSDT");
    public static final List<String> HUOBI_SUPPORTED_PAIRS = List.of("ethusdt", "btcusdt");

    private CryptoTradingConstants() {
        // Private constructor to prevent instantiation
    }
}
