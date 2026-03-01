package com.hong_phat.crypto_trading.config;

import com.hong_phat.crypto_trading.domain.entity.UserEntity;
import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import com.hong_phat.crypto_trading.repository.UserRepository;
import com.hong_phat.crypto_trading.repository.WalletRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * The type Data initializer.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class InitDataConfig {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    /**
     * Initialize default user and wallets.
     */
    @PostConstruct
    public void initData() {
        UserEntity userEntity = UserEntity.builder()
                .username("hong_phat")
                .build();
        userEntity = userRepository.save(userEntity);
        log.info("Created default user with id: {}", userEntity.getId());

        WalletEntity usdtWalletEntity = WalletEntity.builder()
                .userId(userEntity.getId())
                .currency("USDT")
                .balance(new BigDecimal("50000"))
                .build();
        walletRepository.save(usdtWalletEntity);

        WalletEntity ethWalletEntity = WalletEntity.builder()
                .userId(userEntity.getId())
                .currency("ETH")
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(ethWalletEntity);

        WalletEntity btcWalletEntity = WalletEntity.builder()
                .userId(userEntity.getId())
                .currency("BTC")
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(btcWalletEntity);

        log.info("Initialized wallets for user {} - USDT: 50000, ETH: 0, BTC: 0", userEntity.getId());
    }
}
