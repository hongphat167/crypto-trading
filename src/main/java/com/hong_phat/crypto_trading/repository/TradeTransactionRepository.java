package com.hong_phat.crypto_trading.repository;

import com.hong_phat.crypto_trading.domain.entity.TradeTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Trade repository.
 */
@Repository
public interface TradeTransactionRepository extends JpaRepository<TradeTransactionEntity, Long> {

    /**
     * Find all by user id order by timestamp desc list.
     *
     * @param userId the user id
     * @return the list
     */
    List<TradeTransactionEntity> findAllByUserIdOrderByCreatedDateDesc(Long userId);
}
