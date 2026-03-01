package com.hong_phat.crypto_trading.schedule.aggregator;

import com.hong_phat.crypto_trading.dto.PriceData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * The type Price aggregator.
 */
@Component
public class PriceAggregator {

    /**
     * Gets best bid.
     *
     * @param priceSources the price sources
     * @return the best bid
     */
    public BigDecimal getBestBid(List<PriceData> priceSources) {
        return priceSources.stream()
                .filter(Objects::nonNull)
                .map(PriceData::getBidPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::max)
                .orElse(null);
    }


    /**
     * Gets best ask.
     *
     * @param priceSources the price sources
     * @return the best ask
     */
    public BigDecimal getBestAsk(List<PriceData> priceSources) {
        return priceSources.stream()
                .filter(Objects::nonNull)
                .map(PriceData::getAskPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::min)
                .orElse(null);
    }
}
