package com.example.orders.orders;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** PP8: expira pedidos NEW antigos. {@code initialDelay} evita rodar no start (e nos testes). */
@Component
class OrderExpirer {
    private final OrderService service;
    private final Duration age;
    OrderExpirer(OrderService service, @Value("${orders.expiry-minutes}") long minutes) {
        this.service = service; this.age = Duration.ofMinutes(minutes);
    }

    @Scheduled(initialDelayString = "${orders.expiry-check-interval}", fixedDelayString = "${orders.expiry-check-interval}")
    void run() { service.expireOlderThan(age); }
}
