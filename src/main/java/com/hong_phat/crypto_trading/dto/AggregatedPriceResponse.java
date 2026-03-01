package com.hong_phat.crypto_trading.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The type Aggregated price response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregatedPriceResponse implements Serializable {

    private String tradingPair;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdDate;
}
