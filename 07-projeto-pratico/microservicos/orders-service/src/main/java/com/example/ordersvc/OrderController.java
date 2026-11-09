package com.example.ordersvc;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@RestController
class OrderController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderController.class);

    record NewOrder(long productId, int quantity) {}

    private final OrderService orders;
    private final CatalogGateway catalog;

    OrderController(OrderService orders, CatalogGateway catalog) { this.orders = orders; this.catalog = catalog; }

    @PostMapping("/orders")
    ResponseEntity<OrderService.Order> create(@RequestBody NewOrder req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.place(req.productId(), req.quantity()));
    }

    @GetMapping("/orders/{id}")
    ResponseEntity<OrderService.Order> get(@PathVariable long id) {
        return orders.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/stats")
    Map<String, Object> stats() {
        return Map.of("outboxPending", orders.pendentesNaOutbox(), "catalogCalls", catalog.chamadas());
    }

    /** Catalog fora do ar / lento / circuito aberto => 503 (o chamador sabe que pode tentar depois). */
    @ExceptionHandler({HttpServerErrorException.class, ResourceAccessException.class, RuntimeException.class})
    ResponseEntity<Map<String, String>> indisponivel(RuntimeException e) {
        Throwable c = e; StringBuilder cadeia = new StringBuilder();
        for (int i = 0; c != null && i < 6; i++, c = c.getCause()) cadeia.append(i == 0 ? "" : " <- ").append(c.getClass().getSimpleName()).append(": ").append(c.getMessage());
        log.warn("chamada ao catalog falhou: {}", cadeia);
        Throwable raiz = e;
        while (raiz.getCause() != null && !(raiz instanceof HttpClientErrorException) && !(raiz instanceof IllegalArgumentException)
               && !(raiz instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException)) raiz = raiz.getCause();
        if (raiz instanceof RuntimeException r) e = r;
        if (e instanceof IllegalArgumentException) return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        if (e instanceof HttpClientErrorException.NotFound) return ResponseEntity.status(404).body(Map.of("error", "produto inexistente"));
        return ResponseEntity.status(503).body(Map.of("error", "catalog indisponível", "cause", e.getClass().getSimpleName()));
    }
}
