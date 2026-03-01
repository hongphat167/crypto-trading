package com.hong_phat.crypto_trading.repository;

import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The interface Aggregated price repository.
 */
@Repository
public interface AggregatedPriceRepository extends JpaRepository<AggregatedPriceEntity, Long> {

    /**
     * Find top by trading pair order by created date desc optional.
     *
     * @param tradingPair the trading pair
     * @return the optional
     */
    Optional<AggregatedPriceEntity> findTopByTradingPairOrderByCreatedDateDesc(TradingPair tradingPair);
}
