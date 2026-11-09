package dev.estudos.cloud.tracing;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/** CL08-08: usa o {@code RestClient.Builder} INJETADO (instrumentado), não {@code RestClient.create()}. */
@RestController
@Profile("orders")
class OrdersController {
    private final RestClient http;

    OrdersController(RestClient.Builder builder,
                     @org.springframework.beans.factory.annotation.Value("${catalog.url:http://localhost:8082}") String url) {
        this.http = builder.baseUrl(url).build();
    }

    @GetMapping("/orders/{id}")
    Map<String, Object> order(@PathVariable long id) {
        var p = http.get().uri("/products/{id}", id).retrieve().body(Map.class);
        return Map.of("orderId", id, "product", p);
    }
}
