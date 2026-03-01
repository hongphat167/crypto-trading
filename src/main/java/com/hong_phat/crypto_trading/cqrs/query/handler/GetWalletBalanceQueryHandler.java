package com.hong_phat.crypto_trading.cqrs.query.handler;

import com.hong_phat.crypto_trading.cqrs.query.GetWalletBalanceQuery;
import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import com.hong_phat.crypto_trading.domain.enums.TradingPair;
import com.hong_phat.crypto_trading.dto.response.WalletResponse;
import com.hong_phat.crypto_trading.exception.CryptoTradingException;
import com.hong_phat.crypto_trading.repository.AggregatedPriceRepository;
import com.hong_phat.crypto_trading.repository.UserRepository;
import com.hong_phat.crypto_trading.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.CURRENCY_TO_PAIR;
import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.USDT;
import static com.hong_phat.crypto_trading.domain.enums.ErrorCode.USER_NOT_FOUND;

/**
 * The type Get wallet balance query handler.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWalletBalanceQueryHandler {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final AggregatedPriceRepository aggregatedPriceRepository;

    /**
     * Handle wallet response.
     *
     * @param query the query
     * @return the wallet response
     */
    public WalletResponse handle(GetWalletBalanceQuery query) {

        validateUser(query.getUserId());

        List<WalletEntity> wallets = walletRepository.findAllByUserId(query.getUserId());

        List<WalletResponse.AssetBalance> assetBalances = wallets.stream()
                .map(wallet -> {
                    BigDecimal valueInUsdt = calculateValueInUsdt(wallet.getCurrency(), wallet.getBalance());
                    return WalletResponse.AssetBalance.builder()
                            .currency(wallet.getCurrency())
                            .balance(wallet.getBalance())
                            .valueInUsdt(valueInUsdt)
                            .build();
                })
                .toList();

        BigDecimal totalBalanceInUsdt = assetBalances.stream()
                .map(WalletResponse.AssetBalance::getValueInUsdt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return WalletResponse.builder()
                .totalBalanceInUsdt(totalBalanceInUsdt)
                .assetBalances(assetBalances)
                .build();
    }

    private BigDecimal calculateValueInUsdt(String currency, BigDecimal balance) {
        if (USDT.equals(currency)) {
            return balance;
        }

        TradingPair pair = CURRENCY_TO_PAIR.get(currency);
        if (pair == null) {
            return BigDecimal.ZERO;
        }

        return aggregatedPriceRepository.findTopByTradingPairOrderByCreatedDateDesc(pair)
                .map(price -> balance.multiply(price.getBidPrice()))
                .orElse(BigDecimal.ZERO);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CryptoTradingException(USER_NOT_FOUND.getCode(), USER_NOT_FOUND.getMessage());
        }
    }
}
