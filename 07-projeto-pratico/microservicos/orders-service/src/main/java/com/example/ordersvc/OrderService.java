package com.example.ordersvc;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
class OrderService {
    record Order(long id, long productId, int quantity, long totalCents, String currency) {}

    private final JdbcClient jdbc;
    private final CatalogGateway catalog;
    private final JsonMapper json = JsonMapper.builder().build();

    OrderService(JdbcClient jdbc, CatalogGateway catalog) { this.jdbc = jdbc; this.catalog = catalog; }

    /** A chamada ao catalog fica FORA da transação (não segura conexão do banco durante I/O remoto). */
    Order place(long productId, int quantity) {
        var p = catalog.find(productId);
        if (p.stock() < quantity) throw new IllegalArgumentException("estoque insuficiente");
        return save(productId, quantity, p.priceCents() * quantity, p.currency());
    }

    /** Pedido + linha da outbox na MESMA transação (PP12). */
    @Transactional
    Order save(long productId, int quantity, long total, String currency) {
        var keys = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.sql("INSERT INTO orders(product_id, quantity, total_cents, currency) VALUES (?,?,?,?)")
            .params(productId, quantity, total, currency).update(keys, "id");
        long id = keys.getKey().longValue();
        jdbc.sql("INSERT INTO outbox(topic, payload) VALUES (?,?)").params("order-created",
            json.writeValueAsString(java.util.Map.of("orderId", id, "productId", productId, "quantity", quantity))).update();
        return new Order(id, productId, quantity, total, currency);
    }

    java.util.Optional<Order> find(long id) {
        return jdbc.sql("SELECT id, product_id, quantity, total_cents, currency FROM orders WHERE id = ?").param(id)
            .query((rs, n) -> new Order(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getLong(4), rs.getString(5))).optional();
    }

    int pendentesNaOutbox() { return jdbc.sql("SELECT count(*) FROM outbox WHERE NOT sent").query(Integer.class).single(); }
}
