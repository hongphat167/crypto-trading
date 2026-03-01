package com.hong_phat.crypto_trading.schedule.strategy.impl;

import com.hong_phat.crypto_trading.domain.enums.ExchangeName;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.dto.response.BinanceResponse;
import com.hong_phat.crypto_trading.schedule.strategy.PriceFetchingStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.BINANCE_URL;
import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.SUPPORTED_PAIRS;

/**
 * The type Binance price fetching strategy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BinancePriceFetchingStrategy implements PriceFetchingStrategy {

    private final WebClient webClient;

    @Override
    public Map<String, PriceData> fetchPrices() {
        Map<String, PriceData> prices = new HashMap<>();
        try {
            List<BinanceResponse> binanceResponses = webClient.get()
                    .uri(BINANCE_URL)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<@NonNull List<BinanceResponse>>() {
                    })
                    .block();

            if (Objects.isNull(binanceResponses)) {
                log.warn("No price data received from {}", getExchangeName());
                return prices;
            }

            binanceResponses.stream()
                    .filter(ticker -> SUPPORTED_PAIRS.contains(ticker.getSymbol().toLowerCase()))
                    .forEach(ticker -> {
                        PriceData priceData = PriceData.builder()
                                .askPrice(new BigDecimal(ticker.getAskPrice()))
                                .bidPrice(new BigDecimal(ticker.getBidPrice()))
                                .build();
                        prices.put(ticker.getSymbol().toLowerCase(), priceData);
                    });

            log.info("Fetched {} prices from {}", prices.size(), getExchangeName());
        } catch (Exception e) {
            log.error("Error fetching {} prices: {}", getExchangeName(), e.getMessage());
        }
        return prices;
    }

    @Override
    public ExchangeName getExchangeName() {
        return ExchangeName.BINANCE;
    }
}
