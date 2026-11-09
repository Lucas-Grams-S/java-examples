package com.example.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PP11/PP12: serviço de catálogo extraído do monólito. Banco PRÓPRIO (H2 em memória), configuração do Config Server,
 * registro no Eureka e consumidor idempotente de {@code OrderCreated} (reserva de estoque).
 */
@SpringBootApplication
public class CatalogApplication {
    public static void main(String[] args) { SpringApplication.run(CatalogApplication.class, args); }
}
