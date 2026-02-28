package com.hong_phat.crypto_trading.dto.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Huobi response.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HuobiResponse implements Serializable {

    @Builder.Default
    private List<HuobiTickerData> data = new ArrayList<>();

    @Data
    public static class HuobiTickerData implements Serializable {
        private String symbol;
        private BigDecimal bid;
        private BigDecimal ask;
    }
}
