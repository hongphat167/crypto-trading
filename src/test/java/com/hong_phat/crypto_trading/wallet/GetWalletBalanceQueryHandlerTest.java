package com.hong_phat.crypto_trading.wallet;

import com.hong_phat.crypto_trading.cqrs.query.GetWalletBalanceQuery;
import com.hong_phat.crypto_trading.cqrs.query.handler.GetWalletBalanceQueryHandler;
import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.WalletResponse;
import com.hong_phat.crypto_trading.exception.CryptoTradingException;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import com.hong_phat.crypto_trading.repository.UserRepository;
import com.hong_phat.crypto_trading.repository.WalletRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * The type Get wallet balance query handler test.
 */
@ExtendWith(MockitoExtension.class)
public class GetWalletBalanceQueryHandlerTest {

    private static final Long USER_ID = 1L;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AggregatedPriceRepository aggregatedPriceRepository;
    @InjectMocks
    private GetWalletBalanceQueryHandler getWalletBalanceQueryHandler;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        lenient().when(userRepository.existsById(anyLong())).thenReturn(true);
    }

    /**
     * Handle user not found throws exception.
     */
    @Test
    @DisplayName("Should throw exception when user not found")
    void handle_userNotFound_throwsException() {
        Long invalidUserId = 999L;
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(invalidUserId)
                .build();

        assertThatThrownBy(() -> getWalletBalanceQueryHandler.handle(query))
                .isInstanceOf(CryptoTradingException.class)
                .hasMessage("User not found");
    }

    /**
     * Handle no wallets returns empty response.
     */
    @Test
    @DisplayName("Should return empty wallet when user has no assets")
    void handle_noWallets_returnsEmptyResponse() {
        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAssetBalances()).isEmpty();
    }

    /**
     * Handle usdt only returns direct balance.
     */
    @Test
    @DisplayName("Should return USDT balance without conversion")
    void handle_usdtOnly_returnsDirectBalance() {
        WalletEntity usdtWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("USDT")
                .balance(new BigDecimal("50000"))
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(usdtWallet));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(response.getAssetBalances()).hasSize(1);
        assertThat(response.getAssetBalances().get(0).getCurrency()).isEqualTo("USDT");
        assertThat(response.getAssetBalances().get(0).getValueInUsdt()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    /**
     * Handle eth balance converts to usdt.
     */
    @Test
    @DisplayName("Should convert ETH balance to USDT using bid price")
    void handle_ethBalance_convertsToUsdt() {
        WalletEntity ethWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("ETH")
                .balance(new BigDecimal("2"))
                .build();

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500"))
                .askPrice(new BigDecimal("2501"))
                .createdDate(LocalDateTime.now())
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(ethWallet));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(response.getAssetBalances().get(0).getValueInUsdt()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    /**
     * Handle btc balance converts to usdt.
     */
    @Test
    @DisplayName("Should convert BTC balance to USDT using bid price")
    void handle_btcBalance_convertsToUsdt() {
        WalletEntity btcWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("BTC")
                .balance(new BigDecimal("0.5"))
                .build();

        AggregatedPriceEntity btcPrice = AggregatedPriceEntity.builder()
                .tradingPair(TradingPair.BTCUSDT)
                .bidPrice(new BigDecimal("65000"))
                .askPrice(new BigDecimal("65010"))
                .createdDate(LocalDateTime.now())
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(btcWallet));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.of(btcPrice));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(new BigDecimal("32500"));
    }

    /**
     * Handle multiple assets calculates total.
     */
    @Test
    @DisplayName("Should calculate total balance from multiple assets")
    void handle_multipleAssets_calculatesTotal() {
        List<WalletEntity> wallets = List.of(
                WalletEntity.builder().userId(USER_ID).currency("USDT").balance(new BigDecimal("10000")).build(),
                WalletEntity.builder().userId(USER_ID).currency("ETH").balance(new BigDecimal("2")).build(),
                WalletEntity.builder().userId(USER_ID).currency("BTC").balance(new BigDecimal("0.1")).build()
        );

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500"))
                .askPrice(new BigDecimal("2501"))
                .createdDate(LocalDateTime.now())
                .build();

        AggregatedPriceEntity btcPrice = AggregatedPriceEntity.builder()
                .tradingPair(TradingPair.BTCUSDT)
                .bidPrice(new BigDecimal("65000"))
                .askPrice(new BigDecimal("65010"))
                .createdDate(LocalDateTime.now())
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(wallets);
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                .thenReturn(Optional.of(btcPrice));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        // 10000 + (2 * 2500) + (0.1 * 65000) = 10000 + 5000 + 6500 = 21500
        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(new BigDecimal("21500"));
        assertThat(response.getAssetBalances()).hasSize(3);
    }

    /**
     * Handle price not available returns zero value.
     */
    @Test
    @DisplayName("Should return zero value when price not available for crypto")
    void handle_priceNotAvailable_returnsZeroValue() {
        WalletEntity ethWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("ETH")
                .balance(new BigDecimal("5"))
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(ethWallet));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.empty());

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAssetBalances().getFirst().getValueInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Handle unsupported currency returns zero value.
     */
    @Test
    @DisplayName("Should return zero value for unsupported currency")
    void handle_unsupportedCurrency_returnsZeroValue() {
        WalletEntity unknownWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("DOGE")
                .balance(new BigDecimal("1000"))
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(unknownWallet));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAssetBalances().getFirst().getValueInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    /**
     * Handle zero balance returns zero value.
     */
    @Test
    @DisplayName("Should handle zero balance correctly")
    void handle_zeroBalance_returnsZeroValue() {
        WalletEntity zeroWallet = WalletEntity.builder()
                .userId(USER_ID)
                .currency("ETH")
                .balance(BigDecimal.ZERO)
                .build();

        AggregatedPriceEntity ethPrice = AggregatedPriceEntity.builder()
                .tradingPair(TradingPair.ETHUSDT)
                .bidPrice(new BigDecimal("2500"))
                .askPrice(new BigDecimal("2501"))
                .createdDate(LocalDateTime.now())
                .build();

        when(walletRepository.findAllByUserId(USER_ID)).thenReturn(List.of(zeroWallet));
        when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                .thenReturn(Optional.of(ethPrice));

        GetWalletBalanceQuery query = GetWalletBalanceQuery.builder()
                .userId(USER_ID)
                .build();

        WalletResponse response = getWalletBalanceQueryHandler.handle(query);

        assertThat(response.getTotalBalanceInUsdt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAssetBalances().getFirst().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
