package com.hong_phat.crypto_trading.service;

import com.hong_phat.crypto_trading.domain.entity.TradeTransactionEntity;
import com.hong_phat.crypto_trading.domain.enums.TradeTransactionStatus;
import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.repository.TradeTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Trade transaction service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeTransactionService {

    private final TradeTransactionRepository tradeTransactionRepository;

    /**
     * Save failed transaction.
     *
     * @param userId        the user id
     * @param pair          the pair
     * @param tradeType     the trade type
     * @param quantity      the quantity
     * @param failureReason the failure reason
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransaction(Long userId, TradingPair pair, TradeType tradeType,
                                      BigDecimal quantity, String failureReason) {
        TradeTransactionEntity entity = TradeTransactionEntity.builder()
                .userId(userId)
                .tradingPair(pair)
                .tradeType(tradeType)
                .quantity(quantity)
                .price(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .status(TradeTransactionStatus.FAILED)
                .failureReason(failureReason)
                .createdDate(LocalDateTime.now())
                .build();

        tradeTransactionRepository.save(entity);

        log.warn("Failed trade recorded: {} {} {} - reason: {}", tradeType, quantity, pair, failureReason);
    }
}
