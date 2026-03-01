package com.hong_phat.crypto_trading.trade;

import com.hong_phat.crypto_trading.cqrs.command.ExecuteTradeCommand;
import com.hong_phat.crypto_trading.cqrs.command.handler.ExecuteTradeCommandHandler;
import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.entity.TradeTransactionEntity;
import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import com.hong_phat.crypto_trading.domain.enums.TradeTransactionStatus;
import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.TradeTransactionResponse;
import com.hong_phat.crypto_trading.exception.CryptoTradingException;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import com.hong_phat.crypto_trading.repository.TradeTransactionRepository;
import com.hong_phat.crypto_trading.repository.UserRepository;
import com.hong_phat.crypto_trading.repository.WalletRepository;
import com.hong_phat.crypto_trading.service.TradeTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The type Execute trade command handler test.
 */
@ExtendWith(MockitoExtension.class)
public class ExecuteTradeCommandHandlerTest {

    private static final Long USER_ID = 1L;
    private static final BigDecimal QUANTITY = new BigDecimal("0.5");
    private static final BigDecimal ASK_PRICE = new BigDecimal("2500.00");
    private static final BigDecimal BID_PRICE = new BigDecimal("2490.00");

    @Mock
    private AggregatedPriceRepository aggregatedPriceRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TradeTransactionRepository tradeTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TradeTransactionService tradeTransactionService;

    @InjectMocks
    private ExecuteTradeCommandHandler executeTradeCommandHandler;

    @Captor
    private ArgumentCaptor<WalletEntity> walletCaptor;

    @Captor
    private ArgumentCaptor<TradeTransactionEntity> transactionCaptor;

    private ExecuteTradeCommand buyCommand;

    private ExecuteTradeCommand sellCommand;

    private AggregatedPriceEntity aggregatedPriceEntity;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        buyCommand = ExecuteTradeCommand.builder()
                .userId(USER_ID)
                .tradingPair(TradingPair.ETHUSDT)
                .tradeType(TradeType.BUY)
                .quantity(QUANTITY)
                .build();

        sellCommand = ExecuteTradeCommand.builder()
                .userId(USER_ID)
                .tradingPair(TradingPair.ETHUSDT)
                .tradeType(TradeType.SELL)
                .quantity(QUANTITY)
                .build();

        aggregatedPriceEntity = AggregatedPriceEntity.builder()
                .id(1L)
                .tradingPair(TradingPair.ETHUSDT)
                .askPrice(ASK_PRICE)
                .bidPrice(BID_PRICE)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private WalletEntity createWallet(String currency, BigDecimal balance) {
        return WalletEntity.builder()
                .id(1L)
                .userId(ExecuteTradeCommandHandlerTest.USER_ID)
                .currency(currency)
                .balance(balance)
                .build();
    }

    /**
     * The type User validation tests.
     */
    @Nested
    @DisplayName("User Validation Tests")
    class UserValidationTests {

        /**
         * Handle user not found throws exception.
         */
        @Test
        @DisplayName("Should throw exception when user not found")
        void handle_userNotFound_throwsException() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(buyCommand))
                    .isInstanceOf(CryptoTradingException.class)
                    .hasMessageContaining("User not found");

            verify(tradeTransactionRepository, never()).save(any());
        }
    }

    /**
     * The type Price validation tests.
     */
    @Nested
    @DisplayName("Price Validation Tests")
    class PriceValidationTests {

        /**
         * Handle price not available throws exception.
         */
        @Test
        @DisplayName("Should throw exception when price not available")
        void handle_priceNotAvailable_throwsException() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(buyCommand))
                    .isInstanceOf(CryptoTradingException.class)
                    .hasMessageContaining("Price not available");

            verify(tradeTransactionService).saveFailedTransaction(
                    eq(USER_ID), eq(TradingPair.ETHUSDT), eq(TradeType.BUY), eq(QUANTITY), anyString());
        }
    }

    /**
     * The type Buy trade tests.
     */
    @Nested
    @DisplayName("Buy Trade Tests")
    class BuyTradeTests {

        /**
         * Handle buy trade success.
         */
        @Test
        @DisplayName("Should execute buy trade successfully")
        void handle_buyTrade_success() {
            BigDecimal usdtBalance = new BigDecimal("5000.00");
            BigDecimal expectedTotalAmount = ASK_PRICE.multiply(QUANTITY);

            WalletEntity usdtWallet = createWallet("USDT", usdtBalance);
            WalletEntity ethWallet = createWallet("ETH", BigDecimal.ZERO);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.of(usdtWallet));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "ETH"))
                    .thenReturn(Optional.of(ethWallet));
            when(walletRepository.save(any(WalletEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(tradeTransactionRepository.save(any(TradeTransactionEntity.class)))
                    .thenAnswer(i -> {
                        TradeTransactionEntity entity = i.getArgument(0);
                        return TradeTransactionEntity.builder()
                                .id(1L)
                                .userId(entity.getUserId())
                                .tradingPair(entity.getTradingPair())
                                .tradeType(entity.getTradeType())
                                .price(entity.getPrice())
                                .quantity(entity.getQuantity())
                                .totalAmount(entity.getTotalAmount())
                                .status(entity.getStatus())
                                .createdDate(entity.getCreatedDate())
                                .build();
                    });

            TradeTransactionResponse response = executeTradeCommandHandler.handle(buyCommand);

            assertThat(response).isNotNull();
            assertThat(response.getTradeType()).isEqualTo(TradeType.BUY);
            assertThat(response.getPrice()).isEqualByComparingTo(ASK_PRICE);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
            assertThat(response.getStatus()).isEqualTo(TradeTransactionStatus.SUCCESS);

            verify(walletRepository, times(2)).save(walletCaptor.capture());
            var savedWallets = walletCaptor.getAllValues();

            WalletEntity savedUsdtWallet = savedWallets.stream()
                    .filter(w -> "USDT".equals(w.getCurrency()))
                    .findFirst().orElseThrow();
            assertThat(savedUsdtWallet.getBalance())
                    .isEqualByComparingTo(usdtBalance.subtract(expectedTotalAmount));

            WalletEntity savedEthWallet = savedWallets.stream()
                    .filter(w -> "ETH".equals(w.getCurrency()))
                    .findFirst().orElseThrow();
            assertThat(savedEthWallet.getBalance()).isEqualByComparingTo(QUANTITY);
        }

        /**
         * Handle buy trade insufficient usdt balance.
         */
        @Test
        @DisplayName("Should throw exception when insufficient USDT balance for buy")
        void handle_buyTrade_insufficientUsdtBalance() {
            BigDecimal insufficientBalance = new BigDecimal("100.00");
            WalletEntity usdtWallet = createWallet("USDT", insufficientBalance);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.of(usdtWallet));

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(buyCommand))
                    .isInstanceOf(CryptoTradingException.class)
                    .hasMessageContaining("Insufficient USDT balance");

            verify(tradeTransactionService).saveFailedTransaction(
                    eq(USER_ID), eq(TradingPair.ETHUSDT), eq(TradeType.BUY), eq(QUANTITY), anyString());
        }

        /**
         * Handle buy trade creates usdt wallet if not exists.
         */
        @Test
        @DisplayName("Should create wallet if USDT wallet does not exist")
        void handle_buyTrade_createsUsdtWalletIfNotExists() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.empty());
            when(walletRepository.save(any(WalletEntity.class)))
                    .thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(buyCommand))
                    .isInstanceOf(CryptoTradingException.class)
                    .hasMessageContaining("Insufficient USDT balance");

            verify(walletRepository).save(argThat(wallet ->
                    wallet.getUserId().equals(USER_ID) &&
                            "USDT".equals(wallet.getCurrency()) &&
                            wallet.getBalance().compareTo(BigDecimal.ZERO) == 0));
        }
    }

    /**
     * The type Sell trade tests.
     */
    @Nested
    @DisplayName("Sell Trade Tests")
    class SellTradeTests {

        /**
         * Handle sell trade success.
         */
        @Test
        @DisplayName("Should execute sell trade successfully")
        void handle_sellTrade_success() {
            BigDecimal ethBalance = new BigDecimal("1.0");
            BigDecimal expectedTotalAmount = BID_PRICE.multiply(QUANTITY);

            WalletEntity ethWallet = createWallet("ETH", ethBalance);
            WalletEntity usdtWallet = createWallet("USDT", BigDecimal.ZERO);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "ETH"))
                    .thenReturn(Optional.of(ethWallet));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.of(usdtWallet));
            when(walletRepository.save(any(WalletEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(tradeTransactionRepository.save(any(TradeTransactionEntity.class)))
                    .thenAnswer(i -> {
                        TradeTransactionEntity entity = i.getArgument(0);
                        return TradeTransactionEntity.builder()
                                .id(1L)
                                .userId(entity.getUserId())
                                .tradingPair(entity.getTradingPair())
                                .tradeType(entity.getTradeType())
                                .price(entity.getPrice())
                                .quantity(entity.getQuantity())
                                .totalAmount(entity.getTotalAmount())
                                .status(entity.getStatus())
                                .createdDate(entity.getCreatedDate())
                                .build();
                    });

            TradeTransactionResponse response = executeTradeCommandHandler.handle(sellCommand);

            assertThat(response).isNotNull();
            assertThat(response.getTradeType()).isEqualTo(TradeType.SELL);
            assertThat(response.getPrice()).isEqualByComparingTo(BID_PRICE);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
            assertThat(response.getStatus()).isEqualTo(TradeTransactionStatus.SUCCESS);

            verify(walletRepository, times(2)).save(walletCaptor.capture());
            var savedWallets = walletCaptor.getAllValues();

            WalletEntity savedEthWallet = savedWallets.stream()
                    .filter(w -> "ETH".equals(w.getCurrency()))
                    .findFirst().orElseThrow();
            assertThat(savedEthWallet.getBalance())
                    .isEqualByComparingTo(ethBalance.subtract(QUANTITY));

            WalletEntity savedUsdtWallet = savedWallets.stream()
                    .filter(w -> "USDT".equals(w.getCurrency()))
                    .findFirst().orElseThrow();
            assertThat(savedUsdtWallet.getBalance()).isEqualByComparingTo(expectedTotalAmount);
        }

        /**
         * Handle sell trade insufficient crypto balance.
         */
        @Test
        @DisplayName("Should throw exception when insufficient crypto balance for sell")
        void handle_sellTrade_insufficientCryptoBalance() {
            BigDecimal insufficientBalance = new BigDecimal("0.1");
            WalletEntity ethWallet = createWallet("ETH", insufficientBalance);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "ETH"))
                    .thenReturn(Optional.of(ethWallet));

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(sellCommand))
                    .isInstanceOf(CryptoTradingException.class)
                    .hasMessageContaining("Insufficient crypto balance");

            verify(tradeTransactionService).saveFailedTransaction(
                    eq(USER_ID), eq(TradingPair.ETHUSDT), eq(TradeType.SELL), eq(QUANTITY), anyString());
        }
    }

    /**
     * The type Btc trading pair tests.
     */
    @Nested
    @DisplayName("BTC Trading Pair Tests")
    class BtcTradingPairTests {

        /**
         * Handle btc trading pair success.
         */
        @Test
        @DisplayName("Should handle BTC trading pair correctly")
        void handle_btcTradingPair_success() {
            ExecuteTradeCommand btcBuyCommand = ExecuteTradeCommand.builder()
                    .userId(USER_ID)
                    .tradingPair(TradingPair.BTCUSDT)
                    .tradeType(TradeType.BUY)
                    .quantity(new BigDecimal("0.01"))
                    .build();

            AggregatedPriceEntity btcPrice = AggregatedPriceEntity.builder()
                    .tradingPair(TradingPair.BTCUSDT)
                    .askPrice(new BigDecimal("65000.00"))
                    .bidPrice(new BigDecimal("64900.00"))
                    .createdDate(LocalDateTime.now())
                    .build();

            WalletEntity usdtWallet = createWallet("USDT", new BigDecimal("10000.00"));
            WalletEntity btcWallet = createWallet("BTC", BigDecimal.ZERO);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.BTCUSDT))
                    .thenReturn(Optional.of(btcPrice));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.of(usdtWallet));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "BTC"))
                    .thenReturn(Optional.of(btcWallet));
            when(walletRepository.save(any(WalletEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(tradeTransactionRepository.save(any(TradeTransactionEntity.class)))
                    .thenAnswer(i -> {
                        TradeTransactionEntity entity = i.getArgument(0);
                        return TradeTransactionEntity.builder()
                                .id(1L)
                                .userId(entity.getUserId())
                                .tradingPair(entity.getTradingPair())
                                .tradeType(entity.getTradeType())
                                .price(entity.getPrice())
                                .quantity(entity.getQuantity())
                                .totalAmount(entity.getTotalAmount())
                                .status(entity.getStatus())
                                .createdDate(entity.getCreatedDate())
                                .build();
                    });

            TradeTransactionResponse response = executeTradeCommandHandler.handle(btcBuyCommand);

            assertThat(response.getTradingPair()).isEqualTo(TradingPair.BTCUSDT);
            verify(walletRepository).findByUserIdAndCurrency(USER_ID, "BTC");
        }
    }

    /**
     * The type Transaction recording tests.
     */
    @Nested
    @DisplayName("Transaction Recording Tests")
    class TransactionRecordingTests {

        /**
         * Handle saves transaction correctly.
         */
        @Test
        @DisplayName("Should save transaction with correct details")
        void handle_savesTransactionCorrectly() {
            BigDecimal usdtBalance = new BigDecimal("5000.00");
            WalletEntity usdtWallet = createWallet("USDT", usdtBalance);
            WalletEntity ethWallet = createWallet("ETH", BigDecimal.ZERO);

            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.of(aggregatedPriceEntity));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "USDT"))
                    .thenReturn(Optional.of(usdtWallet));
            when(walletRepository.findByUserIdAndCurrency(USER_ID, "ETH"))
                    .thenReturn(Optional.of(ethWallet));
            when(walletRepository.save(any(WalletEntity.class))).thenAnswer(i -> i.getArgument(0));
            when(tradeTransactionRepository.save(any(TradeTransactionEntity.class)))
                    .thenAnswer(i -> {
                        TradeTransactionEntity entity = i.getArgument(0);
                        return TradeTransactionEntity.builder()
                                .id(1L)
                                .userId(entity.getUserId())
                                .tradingPair(entity.getTradingPair())
                                .tradeType(entity.getTradeType())
                                .price(entity.getPrice())
                                .quantity(entity.getQuantity())
                                .totalAmount(entity.getTotalAmount())
                                .status(entity.getStatus())
                                .createdDate(entity.getCreatedDate())
                                .build();
                    });

            executeTradeCommandHandler.handle(buyCommand);

            verify(tradeTransactionRepository).save(transactionCaptor.capture());
            TradeTransactionEntity savedTransaction = transactionCaptor.getValue();

            assertThat(savedTransaction.getUserId()).isEqualTo(USER_ID);
            assertThat(savedTransaction.getTradingPair()).isEqualTo(TradingPair.ETHUSDT);
            assertThat(savedTransaction.getTradeType()).isEqualTo(TradeType.BUY);
            assertThat(savedTransaction.getPrice()).isEqualByComparingTo(ASK_PRICE);
            assertThat(savedTransaction.getQuantity()).isEqualByComparingTo(QUANTITY);
            assertThat(savedTransaction.getStatus()).isEqualTo(TradeTransactionStatus.SUCCESS);
            assertThat(savedTransaction.getCreatedDate()).isNotNull();
        }

        /**
         * Handle records failed transaction.
         */
        @Test
        @DisplayName("Should record failed transaction when trade fails")
        void handle_recordsFailedTransaction() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(TradingPair.ETHUSDT))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> executeTradeCommandHandler.handle(buyCommand))
                    .isInstanceOf(CryptoTradingException.class);

            verify(tradeTransactionService).saveFailedTransaction(
                    eq(USER_ID),
                    eq(TradingPair.ETHUSDT),
                    eq(TradeType.BUY),
                    eq(QUANTITY),
                    anyString());
        }
    }
}
