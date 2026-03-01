package com.hong_phat.crypto_trading.scheudle;

import com.hong_phat.crypto_trading.domain.enums.ExchangeName;
import com.hong_phat.crypto_trading.dto.PriceData;
import com.hong_phat.crypto_trading.dto.response.BinanceResponse;
import com.hong_phat.crypto_trading.schedule.strategy.impl.BinancePriceFetchingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.BINANCE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * The type Binance price fetching strategy test.
 */
@ExtendWith(MockitoExtension.class)
public class BinancePriceFetchingStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private BinancePriceFetchingStrategy binanceStrategy;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        binanceStrategy = new BinancePriceFetchingStrategy(webClient);
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientCall(Mono<?> mono) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(BINANCE_URL))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn((Mono) mono);
    }

    private BinanceResponse createBinanceResponse(String symbol, String bidPrice, String askPrice) {
        BinanceResponse response = new BinanceResponse();
        response.setSymbol(symbol);
        response.setBidPrice(bidPrice);
        response.setAskPrice(askPrice);
        return response;
    }

    /**
     * Gets exchange name returns binance.
     */
    @Test
    @DisplayName("Should return exchange name as BINANCE enum")
    void getExchangeName_returnsBinance() {
        assertThat(binanceStrategy.getExchangeName()).isEqualTo(ExchangeName.BINANCE);
    }

    /**
     * Fetch prices supported pairs returns mapped prices.
     */
    @Test
    @DisplayName("Should fetch and map supported pairs from Binance")
    void fetchPrices_supportedPairs_returnsMappedPrices() {
        List<BinanceResponse> responses = List.of(
                createBinanceResponse("ETHUSDT", "2500.00", "2510.00"),
                createBinanceResponse("BTCUSDT", "65000.00", "65100.00")
        );
        mockWebClientCall(Mono.just(responses));

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

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
        List<BinanceResponse> responses = List.of(
                createBinanceResponse("ETHUSDT", "2500.00", "2510.00"),
                createBinanceResponse("DOGEUSDT", "0.10", "0.11")
        );
        mockWebClientCall(Mono.just(responses));

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

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

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices exception returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when exception occurs")
    void fetchPrices_exception_returnsEmptyMap() {
        when(webClient.get()).thenThrow(new RuntimeException("Connection failed"));

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices empty list returns empty map.
     */
    @Test
    @DisplayName("Should return empty map when response list is empty")
    void fetchPrices_emptyList_returnsEmptyMap() {
        mockWebClientCall(Mono.just(List.of()));

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

        assertThat(result).isEmpty();
    }

    /**
     * Fetch prices mixed case symbols matches correctly.
     */
    @Test
    @DisplayName("Should handle case-insensitive symbol matching")
    void fetchPrices_mixedCaseSymbols_matchesCorrectly() {
        List<BinanceResponse> responses = List.of(
                createBinanceResponse("ETHUSDT", "2500.00", "2510.00"),
                createBinanceResponse("BTCUSDT", "65000.00", "65100.00")
        );
        mockWebClientCall(Mono.just(responses));

        Map<String, PriceData> result = binanceStrategy.fetchPrices();

        assertThat(result).containsKey("ethusdt");
        assertThat(result).containsKey("btcusdt");
    }
}
