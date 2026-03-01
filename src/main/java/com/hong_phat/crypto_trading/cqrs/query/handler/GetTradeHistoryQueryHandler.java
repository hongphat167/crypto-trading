package com.hong_phat.crypto_trading.cqrs.query.handler;

import com.hong_phat.crypto_trading.cqrs.query.GetTradeHistoryQuery;
import com.hong_phat.crypto_trading.dto.response.TradeTransactionResponse;
import com.hong_phat.crypto_trading.exception.CryptoTradingException;
import com.hong_phat.crypto_trading.repository.TradeTransactionRepository;
import com.hong_phat.crypto_trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hong_phat.crypto_trading.domain.enums.ErrorCode.USER_NOT_FOUND;

/**
 * The type Get trade history query handler.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTradeHistoryQueryHandler {

    private final TradeTransactionRepository tradeTransactionRepository;
    private final UserRepository userRepository;

    /**
     * Handle list.
     *
     * @param query the query
     * @return the list
     */
    public List<TradeTransactionResponse> handle(GetTradeHistoryQuery query) {

        if (!userRepository.existsById(query.getUserId())) {
            throw new CryptoTradingException(USER_NOT_FOUND.getCode(), USER_NOT_FOUND.getMessage());
        }

        return tradeTransactionRepository.findAllByUserIdOrderByCreatedDateDesc(query.getUserId())
                .stream()
                .map(trade -> TradeTransactionResponse.builder()
                        .id(trade.getId())
                        .userId(trade.getUserId())
                        .tradingPair(trade.getTradingPair())
                        .tradeType(trade.getTradeType())
                        .price(trade.getPrice())
                        .quantity(trade.getQuantity())
                        .totalAmount(trade.getTotalAmount())
                        .status(trade.getStatus())
                        .createdDate(trade.getCreatedDate())
                        .build())
                .toList();
    }
}
