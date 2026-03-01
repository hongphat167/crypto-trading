package com.hong_phat.crypto_trading.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Get trade history query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTradeHistoryQuery implements Serializable {
    private Long userId;
}
