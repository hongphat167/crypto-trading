package com.hong_phat.crypto_trading.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The type Get wallet balance query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetWalletBalanceQuery implements Serializable {
    private Long userId;
}
