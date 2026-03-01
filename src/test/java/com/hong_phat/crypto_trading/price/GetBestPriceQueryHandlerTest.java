package com.hong_phat.crypto_trading.price;


import com.hong_phat.crypto_trading.cqrs.query.handler.GetBestPriceQueryHandler;
import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.AggregatedPriceResponse;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * The type Get best price query handler test.
 */
@ExtendWith(MockitoExtension.class)
public class GetBestPriceQueryHandlerTest {

    @Mock
    private AggregatedPriceRepository aggregatedPriceRepository;

    private GetBestPriceQueryHandler getBestPriceQueryHandler;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        getBestPriceQueryHandler = new GetBestPriceQueryHandler(aggregatedPriceRepository);
    }

    /**
     * Handle all pairs available returns all prices.
     */
    @Test
    @DisplayName("Should return best prices for all trading pairs when data exists")
    void handle_allPairsAvailable_returnsAllPrices() {
        LocalDateTime now = LocalDateTime.now();

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .id(1L)
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500.00"))
                .askPrice(new BigDecimal("2501.00"))
                .createdDate(now)
                .build();

        AggregatedPriceEntity btcPrice = AggregatedPriceEntity.builder()
                .id(2L)
                .tradingPair(TradingPair.BTCUSDT)
                .bidPrice(new BigDecimal("65000.00"))
                .askPrice(new BigDecimal("65010.00"))
                .createdDate(now)
                .build();

        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.of(btcPrice));

        List<AggregatedPriceResponse> result = getBestPriceQueryHandler.handle();

        assertThat(result).hasSize(2);

        AggregatedPriceResponse ethResponse = result.stream()
                .filter(r -> r.getTradingPair().equals(TradingPair.ETHUSDT.getName()))
                .findFirst().orElseThrow();
        assertThat(ethResponse.getBidPrice()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(ethResponse.getAskPrice()).isEqualByComparingTo(new BigDecimal("2501.00"));
        assertThat(ethResponse.getCreatedDate()).isEqualTo(now);

        AggregatedPriceResponse btcResponse = result.stream()
                .filter(r -> r.getTradingPair().equals(TradingPair.BTCUSDT.getName()))
                .findFirst().orElseThrow();
        assertThat(btcResponse.getBidPrice()).isEqualByComparingTo(new BigDecimal("65000.00"));
        assertThat(btcResponse.getAskPrice()).isEqualByComparingTo(new BigDecimal("65010.00"));
        assertThat(btcResponse.getCreatedDate()).isEqualTo(now);

        verify(aggregatedPriceRepository, times(TradingPair.values().length))
                .findTopByTradingPairOrderByCreatedDateDesc(any(TradingPair.class));
    }

    /**
     * Handle no prices available returns empty list.
     */
    @Test
    @DisplayName("Should return empty list when no price data exists")
    void handle_noPricesAvailable_returnsEmptyList() {
        for (TradingPair pair : TradingPair.values()) {
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(pair))
                    .thenReturn(Optional.empty());
        }

        List<AggregatedPriceResponse> result = getBestPriceQueryHandler.handle();

        assertThat(result).isEmpty();
    }

    /**
     * Handle partial data returns only available pairs.
     */
    @Test
    @DisplayName("Should return only available pairs when some pairs have no data")
    void handle_partialData_returnsOnlyAvailablePairs() {
        LocalDateTime now = LocalDateTime.now();

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .id(1L)
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500.00"))
                .askPrice(new BigDecimal("2501.00"))
                .createdDate(now)
                .build();

        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.empty());

        List<AggregatedPriceResponse> result = getBestPriceQueryHandler.handle();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTradingPair()).isEqualTo(TradingPair.ETHUSDT.getName());
        assertThat(result.getFirst().getBidPrice()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(result.getFirst().getAskPrice()).isEqualByComparingTo(new BigDecimal("2501.00"));
    }

    /**
     * Handle returns latest price.
     */
    @Test
    @DisplayName("Should return the latest price for each trading pair")
    void handle_returnsLatestPrice() {
        LocalDateTime now = LocalDateTime.now();

        AggregatedPriceEntity latestBtcPrice = AggregatedPriceEntity.builder()
                .id(3L)
                .tradingPair(TradingPair.BTCUSDT)
                .bidPrice(new BigDecimal("66000.00"))
                .askPrice(new BigDecimal("66010.00"))
                .createdDate(now)
                .build();

        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.of(latestBtcPrice));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.empty());

        List<AggregatedPriceResponse> result = getBestPriceQueryHandler.handle();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBidPrice()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(result.getFirst().getAskPrice()).isEqualByComparingTo(new BigDecimal("66010.00"));
    }

    /**
     * Handle maps fields correctly.
     */
    @Test
    @DisplayName("Should map trading pair name correctly in response")
    void handle_mapsFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .id(1L)
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500.1234567890"))
                .askPrice(new BigDecimal("2501.9876543210"))
                .createdDate(now)
                .build();

        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.empty());

        List<AggregatedPriceResponse> result = getBestPriceQueryHandler.handle();

        assertThat(result).hasSize(1);
        AggregatedPriceResponse response = result.getFirst();
        assertThat(response.getTradingPair()).isEqualTo(TradingPair.ETHUSDT.getName());
        assertThat(response.getBidPrice()).isEqualByComparingTo(new BigDecimal("2500.1234567890"));
        assertThat(response.getAskPrice()).isEqualByComparingTo(new BigDecimal("2501.9876543210"));
        assertThat(response.getCreatedDate()).isEqualTo(now);
    }

    /**
     * Handle queries repository for each pair.
     */
    @Test
    @DisplayName("Should query repository once per trading pair")
    void handle_queriesRepositoryForEachPair() {
        for (TradingPair pair : TradingPair.values()) {
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(pair))
                    .thenReturn(Optional.empty());
        }

        getBestPriceQueryHandler.handle();

        for (TradingPair pair : TradingPair.values()) {
            verify(aggregatedPriceRepository, times(1))
                    .findTopByTradingPairOrderByCreatedDateDesc(pair);
        }
        verifyNoMoreInteractions(aggregatedPriceRepository);
    }
}
