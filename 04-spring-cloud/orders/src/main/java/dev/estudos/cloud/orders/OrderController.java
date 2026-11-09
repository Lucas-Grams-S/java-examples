package dev.estudos.cloud.orders;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@RestController
class OrderController {
    private final CatalogClient catalog;
    OrderController(CatalogClient c) { this.catalog = c; }

    @GetMapping("/orders/{id}")
    Map<String, Object> get(@PathVariable long id) {
        try {
            return Map.of("orderId", id, "product", catalog.product(id));
        } catch (ResourceAccessException | HttpServerErrorException e) {
            // CL05-09: degradacao (decisao de negocio): pedido sem detalhes do produto
            return Map.of("orderId", id, "product", Map.of("id", id, "unavailable", true));
        }
    }
}
