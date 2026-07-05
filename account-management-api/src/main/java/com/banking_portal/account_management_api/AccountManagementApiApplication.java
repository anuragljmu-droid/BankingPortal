package com.banking_portal.account_management_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AccountManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountManagementApiApplication.class, args);
	}

}
