package com.hong_phat.crypto_trading.schedule.strategy;

import com.hong_phat.crypto_trading.domain.enums.ExchangeName;
import com.hong_phat.crypto_trading.dto.PriceData;

import java.util.Map;

/**
 * The interface Price fetching strategy.
 */
public interface PriceFetchingStrategy {

    /**
     * Fetch prices from the exchange.
     *
     * @return map of symbol to price data
     */
    Map<String, PriceData> fetchPrices();

    /**
     * Get the exchange name.
     *
     * @return the exchange name
     */
    ExchangeName getExchangeName();
}
