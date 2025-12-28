package com.confiance.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.confiance"})
@EnableDiscoveryClient
public class PortfolioServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioServiceApplication.class, args);
    }
}