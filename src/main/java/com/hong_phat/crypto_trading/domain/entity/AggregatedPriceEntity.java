package com.hong_phat.crypto_trading.domain.entity;

import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregated_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trading_pair", nullable = false)
    private TradingPair tradingPair;

    @Column(name = "bid_price", nullable = false, precision = 30, scale = 10)
    private BigDecimal bidPrice;

    @Column(name = "ask_price", nullable = false, precision = 30, scale = 10)
    private BigDecimal askPrice;

    @Column(nullable = false)
    private LocalDateTime createdDate;
}
