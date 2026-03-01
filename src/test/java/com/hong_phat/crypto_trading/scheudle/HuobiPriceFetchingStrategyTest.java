package com.hong_phat.crypto_trading.scheudle;

import com.hong_phat.crypto_trading.domain.enums.ExchangeName;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.dto.response.HuobiResponse;
import com.hong_phat.crypto_trading.schedule.strategy.impl.HuobiPriceFetchingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.HUOBI_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * The type Huobi price fetching strategy test.
 */
@ExtendWith(MockitoExtension.class)
public class HuobiPriceFetchingStrategyTest {
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HuobiPriceFetchingStrategy huobiStrategy;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        huobiStrategy = new HuobiPriceFetchingStrategy(webClient);
    }

    private void mockWebClientCall(Mono<HuobiResponse> mono) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(HUOBI_URL))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(eq(HuobiResponse.class))).thenReturn(mono);
    }

    private HuobiResponse.HuobiTickerData createTicker(String symbol, BigDecimal bid, BigDecimal ask) {
        HuobiResponse.HuobiTickerData ticker = new HuobiResponse.HuobiTickerData();
        ticker.setSymbol(symbol);
        ticker.setBid(bid);
        ticker.setAsk(ask);
        return ticker;
    }

    /**
     * Gets exchange name returns huobi.
     */
    @Test
    @DisplayName("Should return exchange name as HUOBI enum")
    void getExchangeName_returnsHuobi() {
        assertThat(huobiStrategy.getExchangeName()).isEqualTo(ExchangeName.HUOBI);
    }

    /**
     * Fetch prices supported pairs returns mapped prices.
     */
    @Test
    @DisplayName("Should fetch and map supported pairs from Huobi")
    void fetchPrices_supportedPairs_returnsMappedPrices() {
        HuobiResponse response = new HuobiResponse();
        response.setData(List.of(
                createTicker("ethusdt", new BigDecimal("2500.00"), new BigDecimal("2510.00")),
                createTicker("btcusdt", new BigDecimal("65000.00"), new BigDecimal("65100.00"))
        ));
        mockWebClientCall(Mono.just(response));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).hasSize(2);
        assertThat(result.get("ethusdt").getBidPrice()).isEqualByComparingTo("2500.00");
        assertThat(result.get("ethusdt").getAskPrice()).isEqualByComparingTo("2510.00");
        assertThat(result.get("btcusdt").getBidPrice()).isEqualByComparingTo("65000.00");
        assertThat(result.get("btcusdt").getAskPrice()).isEqualByComparingTo("65100.00");
    }

    /**
     * Fetch prices unsupported pairs filtered.
     */
    @Test
    @DisplayName("Should filter out unsupported pairs")
    void fetchPrices_unsupportedPairs_filtered() {
        HuobiResponse response = new HuobiResponse();
        response.setData(List.of(
                createTicker("ethusdt", new BigDecimal("2500.00"), new BigDecimal("2510.00")),
                createTicker("dogeusdt", new BigDecimal("0.10"), new BigDecimal("0.11"))
        ));
        mockWebClientCall(Mono.just(response));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).hasSize(1);
        assertThat(result).containsKey("ethusdt");
        assertThat(result).doesNotContainKey("dogeusdt");
    }

    /**
     * Fetch prices null response returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when response is null")
    void fetchPrices_nullResponse_returnsEmptyMap() {
        mockWebClientCall(Mono.justOrEmpty(null));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices null data returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when data is null")
    void fetchPrices_nullData_returnsEmptyMap() {
        HuobiResponse response = new HuobiResponse();
        response.setData(null);
        mockWebClientCall(Mono.just(response));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices exception returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when exception occurs")
    void fetchPrices_exception_returnsEmptyMap() {
        when(webClient.get()).thenThrow(new RuntimeException("Connection failed"));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices empty data list returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when data list is empty")
    void fetchPrices_emptyDataList_returnsEmptyMap() {
        HuobiResponse response = new HuobiResponse();
        response.setData(List.of());
        mockWebClientCall(Mono.just(response));

        Map<String, PriceData> result = huobiStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }
}
