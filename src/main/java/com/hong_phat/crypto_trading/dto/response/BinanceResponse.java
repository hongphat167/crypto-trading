package com.hong_phat.crypto_trading.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BinanceResponse implements Serializable {
    private String symbol;
    private String bidPrice;
    private String askPrice;
}
