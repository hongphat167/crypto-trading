package com.hong_phat.crypto_trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The type Web client config.
 */
@Configuration
public class WebClientConfig {

    /**
     * Web client web client.
     *
     * @return the web client
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
