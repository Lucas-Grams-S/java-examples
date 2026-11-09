package com.example.catalog;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class CatalogController {
    private final JdbcClient jdbc;
    private final String currency;
    private final AtomicInteger falhas = new AtomicInteger();
    private volatile long atrasoMs;

    CatalogController(JdbcClient jdbc, @Value("${shop.currency:USD}") String currency) {
        this.jdbc = jdbc; this.currency = currency;
    }

    @GetMapping("/products/{id}")
    Map<String, Object> product(@PathVariable long id) throws InterruptedException {
        if (atrasoMs > 0) Thread.sleep(atrasoMs);
        if (falhas.getAndUpdate(n -> n > 0 ? n - 1 : 0) > 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "falha injetada");
        return jdbc.sql("SELECT id, name, price_cents, stock FROM products WHERE id = ?").param(id).query((rs, n) -> Map.<String, Object>of(
                "id", rs.getLong("id"), "name", rs.getString("name"), "priceCents", rs.getLong("price_cents"),
                "stock", rs.getInt("stock"), "currency", currency))
            .optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "produto " + id));
    }

    /** Estoque atual (já descontadas as reservas feitas via Kafka). */
    @GetMapping("/reservations/count")
    Map<String, Object> reservas() {
        return Map.of("reservations", jdbc.sql("SELECT count(*) FROM reservations").query(Integer.class).single());
    }

    // ---- injeção de falhas para o laboratório (PP12) -----------------------------------------------------
    @PostMapping("/admin/falhar/{n}") @ResponseStatus(HttpStatus.NO_CONTENT) void falhar(@PathVariable int n) { falhas.set(n); }
    @PostMapping("/admin/atraso/{ms}") @ResponseStatus(HttpStatus.NO_CONTENT) void atraso(@PathVariable long ms) { atrasoMs = ms; }
}
