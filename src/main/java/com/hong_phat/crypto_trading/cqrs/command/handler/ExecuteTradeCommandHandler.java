package com.hong_phat.crypto_trading.cqrs.command.handler;

import com.hong_phat.crypto_trading.cqrs.command.ExecuteTradeCommand;
import com.hong_phat.crypto_trading.domain.entity.AggregatedPriceEntity;
import com.hong_phat.crypto_trading.domain.entity.TradeTransactionEntity;
import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import com.hong_phat.crypto_trading.domain.enums.ErrorCode;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.CRYPTO_CURRENCY_MAP;
import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.USDT;
import static com.hong_phat.crypto_trading.domain.enums.ErrorCode.*;

/**
 * The type Execute trade command handler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteTradeCommandHandler {

    private final AggregatedPriceRepository aggregatedPriceRepository;
    private final WalletRepository walletRepository;
    private final TradeTransactionRepository tradeTransactionRepository;
    private final UserRepository userRepository;
    private final TradeTransactionService tradeTransactionService;

    /**
     * Handle trade transaction response.
     *
     * @param command the command
     * @return the trade transaction response
     */
    @Transactional
    public TradeTransactionResponse handle(ExecuteTradeCommand command) {
        validateUser(command.getUserId());

        try {
            return executeTrade(command);
        } catch (CryptoTradingException e) {
            handleFailedTrade(command, e);
            throw e;
        }
    }

    private TradeTransactionResponse executeTrade(ExecuteTradeCommand command) {

        BigDecimal price = getTradePrice(command.getTradingPair(), command.getTradeType());
        BigDecimal totalAmount = price.multiply(command.getQuantity());
        String cryptoCurrency = getCryptoCurrency(command.getTradingPair());

        processWalletTransfer(command, totalAmount, cryptoCurrency);

        TradeTransactionEntity transaction = saveTransaction(command, price, totalAmount);

        log.info("Trade executed: {} {} {} at price {} (total: {})",
                command.getTradeType(), command.getQuantity(), command.getTradingPair(), price, totalAmount);

        return toResponse(transaction);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CryptoTradingException(USER_NOT_FOUND.getCode(), USER_NOT_FOUND.getMessage());
        }
    }

    private BigDecimal getTradePrice(TradingPair pair, TradeType tradeType) {

        AggregatedPriceEntity latestPrice = aggregatedPriceRepository
                .findTopByTradingPairOrderByCreatedDateDesc(pair)
                .orElseThrow(() -> new CryptoTradingException(
                        PRICE_NOT_AVAILABLE.getCode(), PRICE_NOT_AVAILABLE.getMessage()));

        return tradeType == TradeType.BUY ? latestPrice.getAskPrice() : latestPrice.getBidPrice();
    }

    private void processWalletTransfer(ExecuteTradeCommand command, BigDecimal totalAmount, String cryptoCurrency) {

        if (command.getTradeType() == TradeType.BUY) {
            debitWallet(command.getUserId(), USDT, totalAmount, INSUFFICIENT_USDT_BALANCE);
            creditWallet(command.getUserId(), cryptoCurrency, command.getQuantity());
        } else {
            debitWallet(command.getUserId(), cryptoCurrency, command.getQuantity(), INSUFFICIENT_CRYPTO_BALANCE);
            creditWallet(command.getUserId(), USDT, totalAmount);
        }
    }

    private void debitWallet(Long userId, String currency, BigDecimal amount, ErrorCode errorCode) {

        WalletEntity walletEntity = getOrCreateWallet(userId, currency);

        if (walletEntity.getBalance().compareTo(amount) < 0) {
            throw new CryptoTradingException(errorCode.getCode(), errorCode.getMessage());
        }

        walletRepository.save(
                walletEntity.toBuilder()
                        .balance(walletEntity.getBalance().subtract(amount))
                        .build()
        );
    }

    private void creditWallet(Long userId, String currency, BigDecimal amount) {
        WalletEntity walletEntity = getOrCreateWallet(userId, currency);

        walletRepository.save(
                walletEntity.toBuilder()
                        .balance(walletEntity.getBalance().add(amount))
                        .build()
        );
    }

    private WalletEntity getOrCreateWallet(Long userId, String currency) {
        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> walletRepository.save(WalletEntity.builder()
                        .userId(userId)
                        .currency(currency)
                        .balance(BigDecimal.ZERO)
                        .build()));
    }

    private TradeTransactionEntity saveTransaction(ExecuteTradeCommand command, BigDecimal price, BigDecimal totalAmount) {

        return tradeTransactionRepository.save(TradeTransactionEntity.builder()
                .userId(command.getUserId())
                .tradingPair(command.getTradingPair())
                .tradeType(command.getTradeType())
                .price(price)
                .quantity(command.getQuantity())
                .totalAmount(totalAmount)
                .status(TradeTransactionStatus.SUCCESS)
                .createdDate(LocalDateTime.now())
                .build());
    }

    private void handleFailedTrade(ExecuteTradeCommand command, CryptoTradingException e) {
        log.error("Trade execution failed: {}", e.getMessage());
        tradeTransactionService.saveFailedTransaction(
                command.getUserId(), command.getTradingPair(),
                command.getTradeType(), command.getQuantity(), e.getMessage());
    }

    private TradeTransactionResponse toResponse(TradeTransactionEntity entity) {
        return TradeTransactionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tradingPair(entity.getTradingPair())
                .tradeType(entity.getTradeType())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    private String getCryptoCurrency(TradingPair pair) {
        return CRYPTO_CURRENCY_MAP.get(pair);
    }
}
