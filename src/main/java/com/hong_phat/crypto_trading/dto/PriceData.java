package com.hong_phat.crypto_trading.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceData implements Serializable {
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
}
