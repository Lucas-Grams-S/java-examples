package dev.estudos.boot.config;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SB03-11 · Injetando e usando as propriedades tipadas num controlador.
 * Documento: 03-configuracao-externalizada.md §5.2.
 *
 * <p>Saída real ({@code POST /orders}): {@code {"currency":"BRL","maxItems":50}};
 * com {@code ORDERS_CURRENCY=EUR} no ambiente, {@code "currency":"EUR"}.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrdersProperties props;

    public OrderController(OrdersProperties props) { this.props = props; }

    @PostMapping
    Map<String, Object> create() {
        return Map.of("currency", props.currency(), "maxItems", props.limits().maxItems());
    }
}
