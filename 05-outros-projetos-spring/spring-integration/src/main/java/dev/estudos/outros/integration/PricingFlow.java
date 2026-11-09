package dev.estudos.outros.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class PricingFlow {
    @Bean IntegrationFlow flow() {
        return IntegrationFlow.from("in")
                .filter(String.class, s -> !s.isBlank())
                .transform(String.class, String::toUpperCase)
                .<String, String>transform(s -> s + "-OK")
                .get();
    }
}
