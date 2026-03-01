package com.hong_phat.crypto_trading.repository;

import com.hong_phat.crypto_trading.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface User repository.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
