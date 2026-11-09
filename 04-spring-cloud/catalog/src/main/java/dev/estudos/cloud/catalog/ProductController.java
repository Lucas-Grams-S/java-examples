package dev.estudos.cloud.catalog;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
class ProductController {
    @Value("${catalog.discount}") int discount;          // vem do Config Server (catalog.yml)
    @Value("${shop.currency}") String currency;          // do application.yml global do repositorio
    @Value("${local.server.port}") String port;          // porta real (com server.port=0, server.port daria "0")

    @GetMapping("/products/{id}")
    Map<String, Object> get(@PathVariable long id) {
        return Map.of("id", id, "name", "Product-" + id,
                      "discount", discount, "currency", currency, "instancePort", port);
    }
}
