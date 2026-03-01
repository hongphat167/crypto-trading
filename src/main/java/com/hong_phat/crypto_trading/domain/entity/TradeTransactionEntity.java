package com.hong_phat.crypto_trading.domain.entity;

import com.hong_phat.crypto_trading.domain.enums.TradeTransactionStatus;
import com.hong_phat.crypto_trading.domain.enums.TradeType;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Trade transaction entity.
 */
@Entity
@Table(name = "trade_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trading_pair", nullable = false)
    private TradingPair tradingPair;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeTransactionStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal price;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal quantity;

    @Column(name = "total_amount", nullable = false, precision = 30, scale = 10)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdDate;
}
