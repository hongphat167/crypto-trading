package com.hong_phat.crypto_trading.scheudle;


import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.schedule.aggregator.PriceAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The type Price aggregation schedule test.
 */
@ExtendWith(MockitoExtension.class)
public class PriceAggregatorTest {

    private PriceAggregator priceAggregator;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        priceAggregator = new PriceAggregator();
    }

    /**
     * Gets best bid multiple sources returns highest.
     */
    @Test
    @DisplayName("Should return highest bid when multiple sources provided")
    void getBestBid_multipleSources_returnsHighest() {
        PriceData binance = PriceData.builder().bidPrice(new BigDecimal("2500.00")).askPrice(new BigDecimal("2510.00")).build();
        PriceData huobi = PriceData.builder().bidPrice(new BigDecimal("2505.00")).askPrice(new BigDecimal("2515.00")).build();

        BigDecimal result = priceAggregator.getBestBid(List.of(binance, huobi));

        assertThat(result).isEqualByComparingTo(new BigDecimal("2505.00"));
    }

    /**
     * Gets best ask multiple sources returns lowest.
     */
    @Test
    @DisplayName("Should return lowest ask when multiple sources provided")
    void getBestAsk_multipleSources_returnsLowest() {
        PriceData binance = PriceData.builder().bidPrice(new BigDecimal("2500.00")).askPrice(new BigDecimal("2510.00")).build();
        PriceData huobi = PriceData.builder().bidPrice(new BigDecimal("2505.00")).askPrice(new BigDecimal("2515.00")).build();

        BigDecimal result = priceAggregator.getBestAsk(List.of(binance, huobi));

        assertThat(result).isEqualByComparingTo(new BigDecimal("2510.00"));
    }

    /**
     * Gets best bid single source returns that bid.
     */
    @Test
    @DisplayName("Should return bid from single source")
    void getBestBid_singleSource_returnsThatBid() {
        PriceData single = PriceData.builder().bidPrice(new BigDecimal("65000.00")).askPrice(new BigDecimal("65100.00")).build();

        BigDecimal result = priceAggregator.getBestBid(List.of(single));

        assertThat(result).isEqualByComparingTo(new BigDecimal("65000.00"));
    }

    /**
     * Gets best ask single source returns that ask.
     */
    @Test
    @DisplayName("Should return ask from single source")
    void getBestAsk_singleSource_returnsThatAsk() {
        PriceData single = PriceData.builder().bidPrice(new BigDecimal("65000.00")).askPrice(new BigDecimal("65100.00")).build();

        BigDecimal result = priceAggregator.getBestAsk(List.of(single));

        assertThat(result).isEqualByComparingTo(new BigDecimal("65100.00"));
    }

    /**
     * Gets best bid empty list returns null.
     */
    @Test
    @DisplayName("Should return null bid when empty list")
    void getBestBid_emptyList_returnsNull() {
        BigDecimal result = priceAggregator.getBestBid(Collections.emptyList());

        assertThat(result).isNull();
    }

    /**
     * Gets best ask empty list returns null.
     */
    @Test
    @DisplayName("Should return null ask when empty list")
    void getBestAsk_emptyList_returnsNull() {
        BigDecimal result = priceAggregator.getBestAsk(Collections.emptyList());

        assertThat(result).isNull();
    }

    /**
     * Gets best bid with null entries filters and returns.
     */
    @Test
    @DisplayName("Should filter out null entries and return best bid")
    void getBestBid_withNullEntries_filtersAndReturns() {
        PriceData valid = PriceData.builder().bidPrice(new BigDecimal("2500.00")).askPrice(new BigDecimal("2510.00")).build();

        BigDecimal result = priceAggregator.getBestBid(List.of(valid));

        assertThat(result).isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    /**
     * Gets best bid three sources returns highest.
     */
    @Test
    @DisplayName("Should return best bid among three sources")
    void getBestBid_threeSources_returnsHighest() {
        PriceData source1 = PriceData.builder().bidPrice(new BigDecimal("100")).askPrice(new BigDecimal("110")).build();
        PriceData source2 = PriceData.builder().bidPrice(new BigDecimal("200")).askPrice(new BigDecimal("210")).build();
        PriceData source3 = PriceData.builder().bidPrice(new BigDecimal("150")).askPrice(new BigDecimal("160")).build();

        BigDecimal result = priceAggregator.getBestBid(List.of(source1, source2, source3));

        assertThat(result).isEqualByComparingTo(new BigDecimal("200"));
    }

    /**
     * Gets best ask three sources returns lowest.
     */
    @Test
    @DisplayName("Should return best ask among three sources")
    void getBestAsk_threeSources_returnsLowest() {
        PriceData source1 = PriceData.builder().bidPrice(new BigDecimal("100")).askPrice(new BigDecimal("110")).build();
        PriceData source2 = PriceData.builder().bidPrice(new BigDecimal("200")).askPrice(new BigDecimal("210")).build();
        PriceData source3 = PriceData.builder().bidPrice(new BigDecimal("150")).askPrice(new BigDecimal("160")).build();

        BigDecimal result = priceAggregator.getBestAsk(List.of(source1, source2, source3));

        assertThat(result).isEqualByComparingTo(new BigDecimal("110"));
    }

    /**
     * Gets best bid equal values returns value.
     */
    @Test
    @DisplayName("Should return value when both sources have equal bid")
    void getBestBid_equalValues_returnsValue() {
        PriceData source1 = PriceData.builder().bidPrice(new BigDecimal("2500.00")).askPrice(new BigDecimal("2510.00")).build();
        PriceData source2 = PriceData.builder().bidPrice(new BigDecimal("2500.00")).askPrice(new BigDecimal("2510.00")).build();

        BigDecimal result = priceAggregator.getBestBid(List.of(source1, source2));

        assertThat(result).isEqualByComparingTo(new BigDecimal("2500.00"));
    }
}