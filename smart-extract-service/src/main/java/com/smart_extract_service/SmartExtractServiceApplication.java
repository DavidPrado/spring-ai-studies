package com.smart_extract_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;

@SpringBootApplication(exclude = { ContextFunctionCatalogAutoConfiguration.class })
public class SmartExtractServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartExtractServiceApplication.class, args);
    }

}
