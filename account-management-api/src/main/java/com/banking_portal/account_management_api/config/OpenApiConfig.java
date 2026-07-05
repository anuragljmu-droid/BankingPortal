package com.banking_portal.account_management_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bankingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Management API")
                        .version("0.0.1")
                        .description("REST API for account balances, transactions, debits, deposits, and currency exchange."));
    }
}
