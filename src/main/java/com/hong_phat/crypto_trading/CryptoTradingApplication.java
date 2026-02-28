package com.hong_phat.crypto_trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The type Crypto trading application.
 */
@SpringBootApplication
@EnableScheduling
public class CryptoTradingApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CryptoTradingApplication.class, args);
    }

}
