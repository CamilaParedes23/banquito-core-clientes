package com.banquito.core.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoreCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreCustomerApplication.class, args);
    }
}
