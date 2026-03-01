package com.hong_phat.crypto_trading.schedule;

import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import com.hong_phat.crypto_trading.schedule.aggregator.PriceAggregator;
import com.hong_phat.crypto_trading.schedule.strategy.PriceFetchingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Price aggregation schedule.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationSchedule {

    private final AggregatedPriceRepository aggregatedPriceRepository;
    private final List<PriceFetchingStrategy> priceFetchingStrategies;
    private final PriceAggregator priceAggregator;

    /**
     * Aggregate prices.
     */
    @Scheduled(fixedRate = 10000)
    public void aggregatePrices() {

        List<Map<String, PriceData>> allExchangePrices = priceFetchingStrategies.stream()
                .map(PriceFetchingStrategy::fetchPrices)
                .toList();

        List<AggregatedPriceEntity> aggregatedPriceEntities = new ArrayList<>();

        for (TradingPair pair : TradingPair.values()) {
            String symbol = pair.name().toLowerCase();

            List<PriceData> priceSources = allExchangePrices.stream()
                    .map(prices -> prices.get(symbol))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (priceSources.isEmpty()) {
                log.warn("No price data available for {}", symbol);
                continue;
            }

            BigDecimal bestBid = priceAggregator.getBestBid(priceSources);
            BigDecimal bestAsk = priceAggregator.getBestAsk(priceSources);

            if (Objects.nonNull(bestBid) && Objects.nonNull(bestAsk)) {
                AggregatedPriceEntity aggregatedPriceEntity = AggregatedPriceEntity.builder()
                        .tradingPair(pair)
                        .bidPrice(bestBid)
                        .askPrice(bestAsk)
                        .createdDate(LocalDateTime.now())
                        .build();

                aggregatedPriceEntities.add(aggregatedPriceEntity);
                log.info("Aggregated price for {}: bid={}, ask={}", symbol, bestBid, bestAsk);
            }
        }

        if (!aggregatedPriceEntities.isEmpty()) {
            aggregatedPriceRepository.saveAll(aggregatedPriceEntities);
        }
    }
}