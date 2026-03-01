package com.hong_phat.crypto_trading.repository;

import com.hong_phat.crypto_trading.domain.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The interface Wallet repository.
 */
@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    /**
     * Find by user id and currency optional.
     *
     * @param userId   the user id
     * @param currency the currency
     * @return the optional
     */
    Optional<WalletEntity> findByUserIdAndCurrency(Long userId, String currency);

}
