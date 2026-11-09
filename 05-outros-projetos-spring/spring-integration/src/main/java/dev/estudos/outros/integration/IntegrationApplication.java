package dev.estudos.outros.integration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

/** OP02-02: fluxo filter -> transform -> transform atrás de um {@code @MessagingGateway}. */
@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
public class IntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }

    @Bean CommandLineRunner demo(Pricing pricing) {
        return args -> System.out.println("price(\"abc\") = " + pricing.price("abc"));
    }
}
