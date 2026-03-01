package com.hong_phat.crypto_trading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse implements Serializable {

    @Schema(description = "Total balance in USDT", example = "55000.00")
    private BigDecimal totalBalanceInUsdt;

    @Schema(description = "List of asset balances")
    private List<AssetBalance> assetBalances;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssetBalance implements Serializable {

        @Schema(description = "Currency code", example = "ETH")
        private String currency;

        @Schema(description = "Available balance", example = "1.5")
        private BigDecimal balance;

        @Schema(description = "Value in USDT based on latest price", example = "3750.00")
        private BigDecimal valueInUsdt;
    }
}
