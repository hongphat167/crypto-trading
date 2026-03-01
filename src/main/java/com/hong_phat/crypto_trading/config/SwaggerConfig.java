package com.hong_phat.crypto_trading.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The type Swagger config.
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Crypto trading open api open api.
     *
     * @return the open api
     */
    @Bean
    public OpenAPI cryptoTradingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Crypto Trading API")
                        .description("API for crypto trading platform - supports BUY/SELL operations for ETHUSDT and BTCUSDT pairs")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hong Phat")
                                .email("hogphat1607@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
