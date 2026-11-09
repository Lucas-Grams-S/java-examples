package com.example.ordersvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PP11/PP12: serviço de pedidos extraído do monólito. Banco PRÓPRIO (não enxerga a tabela de produtos);
 * obtém o produto pelo HTTP Service Client do catalog, com timeout + retry + circuit breaker,
 * e publica {@code OrderCreated} pelo padrão Outbox.
 */
@SpringBootApplication
@EnableScheduling
@EnableResilientMethods
public class OrdersApplication {
    public static void main(String[] args) { SpringApplication.run(OrdersApplication.class, args); }
}
