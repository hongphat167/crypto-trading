package com.hong_phat.crypto_trading.trade;

import com.hong_phat.crypto_trading.cqrs.query.GetTradeHistoryQuery;
import com.hong_phat.crypto_trading.cqrs.query.handler.GetTradeHistoryQueryHandler;
import com.hong_phat.crypto_trading.domain.entity.TradeTransactionEntity;
import com.hong_phat.crypto_trading.domain.enums.TradeTransactionStatus;
import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.TradeTransactionResponse;
import com.hong_phat.crypto_trading.exception.CryptoTradingException;
import com.hong_phat.crypto_trading.repository.TradeTransactionRepository;
import com.hong_phat.crypto_trading.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * The type Get trade history query handler test.
 */
@ExtendWith(MockitoExtension.class)
public class GetTradeHistoryQueryHandlerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private TradeTransactionRepository tradeTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetTradeHistoryQueryHandler getTradeHistoryQueryHandler;

    private GetTradeHistoryQuery getTradeHistoryQuery;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        getTradeHistoryQuery = GetTradeHistoryQuery.builder()
                .userId(USER_ID)
                .build();
    }

    private TradeTransactionEntity createTransaction(Long id, TradingPair pair, TradeType tradeType,
                                                     BigDecimal price, BigDecimal quantity,
                                                     TradeTransactionStatus status, LocalDateTime createdDate) {
        return TradeTransactionEntity.builder()
                .id(id)
                .userId(USER_ID)
                .tradingPair(pair)
                .tradeType(tradeType)
                .price(price)
                .quantity(quantity)
                .totalAmount(price.multiply(quantity))
                .status(status)
                .createdDate(createdDate)
                .build();
    }

    /**
     * Handle user not found throws exception.
     */
    @Test
    @DisplayName("Should throw exception when user not found")
    void handle_userNotFound_throwsException() {
        Long invalidUserId = 999L;
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        GetTradeHistoryQuery invalidQuery = GetTradeHistoryQuery.builder()
                .userId(invalidUserId)
                .build();

        assertThatThrownBy(() -> getTradeHistoryQueryHandler.handle(invalidQuery))
                .isInstanceOf(CryptoTradingException.class)
                .hasMessageContaining("User not found");

        verify(tradeTransactionRepository, never()).findAllByUserIdOrderByCreatedDateDesc(anyLong());
    }

    /**
     * Handle no transactions returns empty list.
     */
    @Test
    @DisplayName("Should return empty list when user has no trade history")
    void handle_noTransactions_returnsEmptyList() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(Collections.emptyList());

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).isEmpty();
        verify(tradeTransactionRepository).findAllByUserIdOrderByCreatedDateDesc(USER_ID);
    }

    /**
     * Handle single transaction returns correctly.
     */
    @Test
    @DisplayName("Should return single trade transaction correctly")
    void handle_singleTransaction_returnsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        TradeTransactionEntity transaction = createTransaction(
                1L, TradingPair.ETHUSDT, TradeType.BUY,
                new BigDecimal("2500.00"), new BigDecimal("0.5"),
                TradeTransactionStatus.SUCCESS, now);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(List.of(transaction));

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).hasSize(1);
        TradeTransactionResponse response = result.getFirst();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getTradingPair()).isEqualTo(TradingPair.ETHUSDT);
        assertThat(response.getTradeType()).isEqualTo(TradeType.BUY);
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.getQuantity()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(response.getStatus()).isEqualTo(TradeTransactionStatus.SUCCESS);
        assertThat(response.getCreatedDate()).isEqualTo(now);
    }

    /**
     * Handle multiple transactions returns all ordered by date desc.
     */
    @Test
    @DisplayName("Should return multiple transactions ordered by date descending")
    void handle_multipleTransactions_returnsAllOrderedByDateDesc() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusHours(1);
        LocalDateTime earliest = now.minusHours(2);

        List<TradeTransactionEntity> transactions = List.of(
                createTransaction(3L, TradingPair.BTCUSDT, TradeType.SELL,
                        new BigDecimal("65000.00"), new BigDecimal("0.01"),
                        TradeTransactionStatus.SUCCESS, now),
                createTransaction(2L, TradingPair.ETHUSDT, TradeType.BUY,
                        new BigDecimal("2500.00"), new BigDecimal("1.0"),
                        TradeTransactionStatus.SUCCESS, earlier),
                createTransaction(1L, TradingPair.ETHUSDT, TradeType.BUY,
                        new BigDecimal("2400.00"), new BigDecimal("0.5"),
                        TradeTransactionStatus.FAILED, earliest)
        );

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(transactions);

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).hasSize(3);
        assertThat(result.getFirst().getId()).isEqualTo(3L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(1L);
        assertThat(result.getFirst().getCreatedDate()).isAfter(result.get(1).getCreatedDate());
        assertThat(result.get(1).getCreatedDate()).isAfter(result.get(2).getCreatedDate());
    }

    /**
     * Handle includes failed transactions.
     */
    @Test
    @DisplayName("Should include failed transactions in history")
    void handle_includesFailedTransactions() {
        LocalDateTime now = LocalDateTime.now();
        TradeTransactionEntity failedTransaction = createTransaction(
                1L, TradingPair.ETHUSDT, TradeType.BUY,
                BigDecimal.ZERO, new BigDecimal("0.5"),
                TradeTransactionStatus.FAILED, now);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(List.of(failedTransaction));

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(TradeTransactionStatus.FAILED);
    }

    /**
     * Handle mixed trading pairs returns all.
     */
    @Test
    @DisplayName("Should return transactions for both ETH and BTC trading pairs")
    void handle_mixedTradingPairs_returnsAll() {
        LocalDateTime now = LocalDateTime.now();

        List<TradeTransactionEntity> transactions = List.of(
                createTransaction(2L, TradingPair.BTCUSDT, TradeType.BUY,
                        new BigDecimal("65000.00"), new BigDecimal("0.01"),
                        TradeTransactionStatus.SUCCESS, now),
                createTransaction(1L, TradingPair.ETHUSDT, TradeType.SELL,
                        new BigDecimal("2500.00"), new BigDecimal("1.0"),
                        TradeTransactionStatus.SUCCESS, now.minusMinutes(5))
        );

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(transactions);

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getTradingPair()).isEqualTo(TradingPair.BTCUSDT);
        assertThat(result.get(1).getTradingPair()).isEqualTo(TradingPair.ETHUSDT);
    }

    /**
     * Handle mixed trade types returns all.
     */
    @Test
    @DisplayName("Should return both BUY and SELL transactions")
    void handle_mixedTradeTypes_returnsAll() {
        LocalDateTime now = LocalDateTime.now();

        List<TradeTransactionEntity> transactions = List.of(
                createTransaction(2L, TradingPair.ETHUSDT, TradeType.SELL,
                        new BigDecimal("2600.00"), new BigDecimal("0.5"),
                        TradeTransactionStatus.SUCCESS, now),
                createTransaction(1L, TradingPair.ETHUSDT, TradeType.BUY,
                        new BigDecimal("2500.00"), new BigDecimal("0.5"),
                        TradeTransactionStatus.SUCCESS, now.minusMinutes(10))
        );

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(transactions);

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getTradeType()).isEqualTo(TradeType.SELL);
        assertThat(result.get(1).getTradeType()).isEqualTo(TradeType.BUY);
    }

    /**
     * Handle maps total amount correctly.
     */
    @Test
    @DisplayName("Should map total amount (price * quantity) correctly")
    void handle_mapsTotalAmountCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal price = new BigDecimal("2500.00");
        BigDecimal quantity = new BigDecimal("2.5");

        TradeTransactionEntity transaction = createTransaction(
                1L, TradingPair.ETHUSDT, TradeType.BUY,
                price, quantity, TradeTransactionStatus.SUCCESS, now);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(List.of(transaction));

        List<TradeTransactionResponse> result = getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        assertThat(result.getFirst().getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("6250.00"));
    }

    /**
     * Handle queries repository exactly once.
     */
    @Test
    @DisplayName("Should query repository exactly once")
    void handle_queriesRepositoryExactlyOnce() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(USER_ID))
                .thenReturn(Collections.emptyList());

        getTradeHistoryQueryHandler.handle(getTradeHistoryQuery);

        verify(tradeTransactionRepository, times(1)).findAllByUserIdOrderByCreatedDateDesc(USER_ID);
        verify(userRepository, times(1)).existsById(USER_ID);
        verifyNoMoreInteractions(tradeTransactionRepository);
    }
}
