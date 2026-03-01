package com.hong_phat.crypto_trading.schedule.strategy.impl;

import com.hong_phat.crypto_trading.domain.enums.ExchangeName;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.dto.response.HuobiResponse;
import com.hong_phat.crypto_trading.schedule.strategy.PriceFetchingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.HUOBI_URL;
import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.SUPPORTED_PAIRS;

/**
 * The type Huobi price fetching strategy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HuobiPriceFetchingStrategy implements PriceFetchingStrategy {

    private final WebClient webClient;

    @Override
    public Map<String, PriceData> fetchPrices() {
        Map<String, PriceData> prices = new HashMap<>();
        try {
            HuobiResponse huobiResponse = webClient.get()
                    .uri(HUOBI_URL)
                    .retrieve()
                    .bodyToMono(HuobiResponse.class)
                    .block();

            if (Objects.isNull(huobiResponse) || Objects.isNull(huobiResponse.getData())) {
                log.warn("No price data received from {}", getExchangeName());
                return prices;
            }

            huobiResponse.getData()
                    .stream()
                    .filter(ticker -> SUPPORTED_PAIRS.contains(ticker.getSymbol().toLowerCase()))
                    .forEach(ticker -> {
                        PriceData priceData = PriceData.builder()
                                .askPrice(ticker.getAsk())
                                .bidPrice(ticker.getBid())
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
        return ExchangeName.HUOBI;
    }
}
