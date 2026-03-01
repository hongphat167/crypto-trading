package com.hong_phat.crypto_trading.cqrs.query.handler;

import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.AggregatedPriceResponse;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Get best price query handler.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBestPriceQueryHandler {

    private final AggregatedPriceRepository aggregatedPriceRepository;

    /**
     * Handle list.
     *
     * @return the list
     */
    public List<AggregatedPriceResponse> handle() {
        List<AggregatedPriceResponse> result = new ArrayList<>();

        for (TradingPair pair : TradingPair.values()) {
            Optional<AggregatedPriceEntity> priceOpt = aggregatedPriceRepository
                    .findTopByTradingPairOrderByCreatedDateDesc(pair);

            priceOpt.ifPresent(price -> result.add(
                    AggregatedPriceResponse.builder()
                            .tradingPair(price.getTradingPair().getName())
                            .bidPrice(price.getBidPrice())
                            .askPrice(price.getAskPrice())
                            .createdDate(price.getCreatedDate())
                            .build())
            );
        }

        return result;
    }
}
