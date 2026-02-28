package com.hong_phat.crypto_trading.schedule;

import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.dto.response.BinanceResponse;
import com.hong_phat.crypto_trading.dto.response.HuobiResponse;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.*;

/**
 * The type Price aggregation schedule.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationSchedule {

    private final AggregatedPriceRepository aggregatedPriceRepository;
    private final WebClient webClient;

    /**
     * Aggregate prices.
     */
    @Scheduled(fixedRate = 10000)
    public void aggregatePrices() {

        Map<String, PriceData> binancePrices = fetchBinancePrices();
        Map<String, PriceData> huobiPrices = fetchHuobiPrices();
        List<AggregatedPriceEntity> aggregatedPriceEntities = new ArrayList<>();

        for (TradingPair pair : TradingPair.values()) {
            String symbol = pair.name();
            PriceData binance = binancePrices.get(symbol);
            PriceData huobi = huobiPrices.get(symbol);

            if (Objects.isNull(binance) && Objects.isNull(huobi)) {
                log.warn("No price data available for {}", symbol);
                continue;
            }


            BigDecimal bestBid = getBestBid(binance, huobi);
            BigDecimal bestAsk = getBestAsk(binance, huobi);

            if (Objects.nonNull(bestBid) && Objects.nonNull(bestAsk)) {
                AggregatedPriceEntity aggregatedPriceEntity = AggregatedPriceEntity.builder()
                        .tradingPair(pair)
                        .bidPrice(bestBid)
                        .askPrice(bestAsk)
                        .createdDate(LocalDateTime.now())
                        .build();

                aggregatedPriceEntities.add(aggregatedPriceEntity);
                log.info("Saved aggregated price for {}: bid={}, ask={}", symbol, bestBid, bestAsk);
            }
        }

        if (!aggregatedPriceEntities.isEmpty()) {
            aggregatedPriceRepository.saveAll(aggregatedPriceEntities);
        }
    }

    private Map<String, PriceData> fetchBinancePrices() {
        Map<String, PriceData> prices = new HashMap<>();
        try {
            List<BinanceResponse> binanceResponses = webClient.get()
                    .uri(BINANCE_URL)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BinanceResponse>>() {
                    })
                    .block();

            if (Objects.isNull(binanceResponses)) {
                log.warn("No price data received from Binance");
                return prices;
            }

            binanceResponses.stream()
                    .filter(ticker -> BINANCE_SUPPORTED_PAIRS.contains(ticker.getSymbol()))
                    .forEach(ticker -> {
                        PriceData priceData = PriceData.builder()
                                .askPrice(new BigDecimal(ticker.getAskPrice()))
                                .bidPrice(new BigDecimal(ticker.getBidPrice()))
                                .build();
                        prices.put(ticker.getSymbol(), priceData);
                    });

            log.info("Fetched {} prices from Binance", prices.size());
        } catch (Exception e) {
            log.error("Error fetching Binance prices: {}", e.getMessage());
        }
        return prices;
    }

    private Map<String, PriceData> fetchHuobiPrices() {
        Map<String, PriceData> prices = new HashMap<>();
        try {
            HuobiResponse huobiResponse = webClient.get()
                    .uri(HUOBI_URL)
                    .retrieve()
                    .bodyToMono(HuobiResponse.class)
                    .block();

            if (Objects.isNull(huobiResponse) || Objects.isNull(huobiResponse.getData())) {
                log.warn("No price data received from Huobi");
                return prices;
            }

            huobiResponse.getData()
                    .stream()
                    .filter(ticker -> HUOBI_SUPPORTED_PAIRS.contains(ticker.getSymbol()))
                    .forEach(ticker -> {
                        PriceData priceData = PriceData.builder()
                                .askPrice(ticker.getAsk())
                                .bidPrice(ticker.getBid())
                                .build();
                        String normalizedSymbol = ticker.getSymbol().toUpperCase();
                        prices.put(normalizedSymbol, priceData);
                    });

            log.info("Fetched {} prices from Huobi", prices.size());
        } catch (Exception e) {
            log.error("Error fetching Huobi prices: {}", e.getMessage());
        }
        return prices;
    }

    private BigDecimal getBestBid(PriceData binance, PriceData huobi) {
        if (Objects.nonNull(binance) && Objects.nonNull(huobi)) {
            return binance.getBidPrice().max(huobi.getBidPrice());
        }
        return Objects.nonNull(binance) ? binance.getBidPrice() :
                (Objects.nonNull(huobi) ? huobi.getBidPrice() : null);
    }

    private BigDecimal getBestAsk(PriceData binance, PriceData huobi) {
        if (Objects.nonNull(binance) && Objects.nonNull(huobi)) {
            return binance.getAskPrice().min(huobi.getAskPrice());
        }
        return Objects.nonNull(binance) ? binance.getAskPrice()
                : (Objects.nonNull(huobi) ? huobi.getAskPrice() : null);
    }
}
