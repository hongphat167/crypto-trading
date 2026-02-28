package com.hong_phat.crypto_trading.scheudle;


import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.BinanceResponse;
import com.hong_phat.crypto_trading.dto.response.HuobiResponse;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import com.hong_phat.crypto_trading.schedule.PriceAggregationSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * The type Price aggregation schedule test.
 */
@ExtendWith(MockitoExtension.class)
public class PriceAggregationScheduleTest {

    @Mock
    private AggregatedPriceRepository aggregatedPriceRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Captor
    private ArgumentCaptor<List<AggregatedPriceEntity>> entitiesCaptor;

    private PriceAggregationSchedule schedule;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        schedule = new PriceAggregationSchedule(aggregatedPriceRepository, webClient);
    }

    private void mockWebClientCall() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    /**
     * Aggregate prices both sources available saves best prices.
     */
    @Test
    @DisplayName("Should save aggregated prices with best bid and ask from both sources")
    void aggregatePrices_bothSourcesAvailable_savesBestPrices() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2501.00").build(),
                BinanceResponse.builder().symbol("BTCUSDT").bidPrice("65000.00").askPrice("65010.00").build()
        );

        HuobiResponse huobiData = new HuobiResponse();
        HuobiResponse.HuobiTickerData ethTicker = new HuobiResponse.HuobiTickerData();
        ethTicker.setSymbol("ethusdt");
        ethTicker.setBid(new BigDecimal("2502.00"));
        ethTicker.setAsk(new BigDecimal("2499.00"));

        HuobiResponse.HuobiTickerData btcTicker = new HuobiResponse.HuobiTickerData();
        btcTicker.setSymbol("btcusdt");
        btcTicker.setBid(new BigDecimal("64999.00"));
        btcTicker.setAsk(new BigDecimal("65015.00"));

        huobiData.setData(List.of(ethTicker, btcTicker));

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(huobiData));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        List<AggregatedPriceEntity> saved = entitiesCaptor.getValue();
        assertThat(saved).hasSize(2);

        AggregatedPriceEntity ethEntity = saved.stream()
                .filter(e -> e.getTradingPair() == TradingPair.ETHUSDT)
                .findFirst().orElseThrow();
        // Best bid = max(2500, 2502) = 2502
        assertThat(ethEntity.getBidPrice()).isEqualByComparingTo(new BigDecimal("2502.00"));
        // Best ask = min(2501, 2499) = 2499
        assertThat(ethEntity.getAskPrice()).isEqualByComparingTo(new BigDecimal("2499.00"));
        assertThat(ethEntity.getCreatedDate()).isNotNull();

        AggregatedPriceEntity btcEntity = saved.stream()
                .filter(e -> e.getTradingPair() == TradingPair.BTCUSDT)
                .findFirst().orElseThrow();
        // Best bid = max(65000, 64999) = 65000
        assertThat(btcEntity.getBidPrice()).isEqualByComparingTo(new BigDecimal("65000.00"));
        // Best ask = min(65010, 65015) = 65010
        assertThat(btcEntity.getAskPrice()).isEqualByComparingTo(new BigDecimal("65010.00"));
    }

    /**
     * Aggregate prices huobi fails uses binance only.
     */
    @Test
    @DisplayName("Should use only Binance prices when Huobi returns error")
    void aggregatePrices_huobiFails_usesBinanceOnly() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2501.00").build(),
                BinanceResponse.builder().symbol("BTCUSDT").bidPrice("65000.00").askPrice("65010.00").build()
        );

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Huobi API error")));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        List<AggregatedPriceEntity> saved = entitiesCaptor.getValue();
        assertThat(saved).hasSize(2);

        AggregatedPriceEntity ethEntity = saved.stream()
                .filter(e -> e.getTradingPair() == TradingPair.ETHUSDT)
                .findFirst().orElseThrow();
        assertThat(ethEntity.getBidPrice()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(ethEntity.getAskPrice()).isEqualByComparingTo(new BigDecimal("2501.00"));
    }


    /**
     * Aggregate prices binance fails uses huobi only.
     */
    @Test
    @DisplayName("Should use only Huobi prices when Binance returns error")
    void aggregatePrices_binanceFails_usesHuobiOnly() {

        HuobiResponse huobiData = new HuobiResponse();
        HuobiResponse.HuobiTickerData ethTicker = new HuobiResponse.HuobiTickerData();
        ethTicker.setSymbol("ethusdt");
        ethTicker.setBid(new BigDecimal("2502.00"));
        ethTicker.setAsk(new BigDecimal("2499.00"));
        huobiData.setData(List.of(ethTicker));

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("Binance API error")));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(huobiData));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        List<AggregatedPriceEntity> saved = entitiesCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getTradingPair()).isEqualTo(TradingPair.ETHUSDT);
        assertThat(saved.get(0).getBidPrice()).isEqualByComparingTo(new BigDecimal("2502.00"));
    }

    /**
     * Aggregate prices both fail does not save.
     */
    @Test
    @DisplayName("Should not save when both sources fail")
    void aggregatePrices_bothFail_doesNotSave() {

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new RuntimeException("Binance error")));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Huobi error")));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository, never()).saveAll(anyList());
    }

    /**
     * Aggregate prices both empty does not save.
     */
    @Test
    @DisplayName("Should not save when both sources return empty data")
    void aggregatePrices_bothEmpty_doesNotSave() {

        HuobiResponse emptyHuobi = new HuobiResponse();
        emptyHuobi.setData(Collections.emptyList());

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Collections.emptyList()));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(emptyHuobi));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository, never()).saveAll(anyList());
    }

    /**
     * Aggregate prices unsupported pairs filtered.
     */
    @Test
    @DisplayName("Should filter out unsupported trading pairs")
    void aggregatePrices_unsupportedPairs_filtered() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2501.00").build(),
                BinanceResponse.builder().symbol("DOGEUSDT").bidPrice("0.10").askPrice("0.11").build()
        );

        HuobiResponse emptyHuobi = new HuobiResponse();
        emptyHuobi.setData(Collections.emptyList());

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(emptyHuobi));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        List<AggregatedPriceEntity> saved = entitiesCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getTradingPair()).isEqualTo(TradingPair.ETHUSDT);
    }

    /**
     * Aggregate prices best bid picks highest.
     */
    @Test
    @DisplayName("Should pick highest bid (best for seller)")
    void aggregatePrices_bestBid_picksHighest() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2700.00").build()
        );

        HuobiResponse huobiData = new HuobiResponse();
        HuobiResponse.HuobiTickerData ethTicker = new HuobiResponse.HuobiTickerData();
        ethTicker.setSymbol("ethusdt");
        ethTicker.setBid(new BigDecimal("2600.00"));
        ethTicker.setAsk(new BigDecimal("2800.00"));
        huobiData.setData(List.of(ethTicker));

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(huobiData));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        AggregatedPriceEntity ethEntity = entitiesCaptor.getValue().stream()
                .filter(e -> e.getTradingPair() == TradingPair.ETHUSDT)
                .findFirst().orElseThrow();
        assertThat(ethEntity.getBidPrice()).isEqualByComparingTo(new BigDecimal("2600.00"));
    }

    /**
     * Aggregate prices best ask picks lowest.
     */
    @Test
    @DisplayName("Should pick lowest ask (best for buyer)")
    void aggregatePrices_bestAsk_picksLowest() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2400.00").askPrice("2501.00").build()
        );

        HuobiResponse huobiData = new HuobiResponse();
        HuobiResponse.HuobiTickerData ethTicker = new HuobiResponse.HuobiTickerData();
        ethTicker.setSymbol("ethusdt");
        ethTicker.setBid(new BigDecimal("2300.00"));
        ethTicker.setAsk(new BigDecimal("2499.00"));
        huobiData.setData(List.of(ethTicker));

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(huobiData));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        AggregatedPriceEntity ethEntity = entitiesCaptor.getValue().stream()
                .filter(e -> e.getTradingPair() == TradingPair.ETHUSDT)
                .findFirst().orElseThrow();
        assertThat(ethEntity.getAskPrice()).isEqualByComparingTo(new BigDecimal("2499.00"));
    }

    /**
     * Aggregate prices consistent timestamp.
     */
    @Test
    @DisplayName("All saved entities should share the same timestamp for consistency")
    void aggregatePrices_consistentTimestamp() {

        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2501.00").build(),
                BinanceResponse.builder().symbol("BTCUSDT").bidPrice("65000.00").askPrice("65010.00").build()
        );

        HuobiResponse emptyHuobi = new HuobiResponse();
        emptyHuobi.setData(Collections.emptyList());

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(emptyHuobi));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        List<AggregatedPriceEntity> saved = entitiesCaptor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getCreatedDate()).isEqualTo(saved.get(1).getCreatedDate());
    }

    /**
     * Aggregate prices huobi null data handled gracefully.
     */
    @Test
    @DisplayName("Should handle Huobi null data gracefully")
    void aggregatePrices_huobiNullData_handledGracefully() {


        List<BinanceResponse> binanceData = List.of(
                BinanceResponse.builder().symbol("ETHUSDT").bidPrice("2500.00").askPrice("2501.00").build()
        );

        HuobiResponse nullDataHuobi = new HuobiResponse();
        nullDataHuobi.setData(null);

        mockWebClientCall();

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(binanceData));
        when(responseSpec.bodyToMono(HuobiResponse.class))
                .thenReturn(Mono.just(nullDataHuobi));

        when(aggregatedPriceRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));


        schedule.aggregatePrices();

        verify(aggregatedPriceRepository).saveAll(entitiesCaptor.capture());
        assertThat(entitiesCaptor.getValue()).hasSize(1);
    }
}
