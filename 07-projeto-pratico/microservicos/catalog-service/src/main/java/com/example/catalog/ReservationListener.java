package com.example.catalog;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * PP12: consome {@code OrderCreated}. A entrega é at-least-once (Outbox), então o consumidor é IDEMPOTENTE:
 * a chave primária de {@code reservations} é o {@code orderId}; repetir o evento não reserva duas vezes.
 */
@Component
class ReservationListener {
    private final JdbcClient jdbc;
    private final JsonMapper json = JsonMapper.builder().build();

    ReservationListener(JdbcClient jdbc) { this.jdbc = jdbc; }

    @KafkaListener(topics = "order-created", groupId = "catalog")
    @Transactional
    void on(String payload) {
        JsonNode e = json.readTree(payload);
        long orderId = e.get("orderId").asLong();
        int novas = jdbc.sql("INSERT INTO reservations(order_id, product_id, quantity) SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM reservations WHERE order_id = ?)")
            .params(orderId, e.get("productId").asLong(), e.get("quantity").asInt(), orderId).update();
        if (novas > 0)
            jdbc.sql("UPDATE products SET stock = stock - ? WHERE id = ?").params(e.get("quantity").asInt(), e.get("productId").asLong()).update();
    }
}
