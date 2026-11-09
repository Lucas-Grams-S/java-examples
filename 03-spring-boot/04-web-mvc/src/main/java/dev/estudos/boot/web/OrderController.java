package dev.estudos.boot.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * SB04-10 · Controlador completo: {@code @Valid @RequestBody} em record, {@code 201 + Location},
 * {@code @PathVariable}, {@code @RequestParam} com default e {@code @ExceptionHandler} devolvendo
 * {@link ProblemDetail}. Documento: 04-web-mvc.md §5.1; respostas do §5.2 (SB04-11) cobertas por {@code OrdersTest}.
 *
 * <p>Saída real (MockMvc, {@code problemdetails.enabled=true}):
 * <pre>
 * POST /orders {"customerId":"c1","total":10.5}
 *  → 201  Location: http://localhost/orders/1
 *         {"id":1,"customerId":"c1","total":10.5,"currency":"BRL"}
 * GET /orders/99
 *  → 404  application/problem+json
 *         {"detail":"Pedido 99 não encontrado","instance":"/orders/99","status":404,"title":"Not Found"}
 * POST /orders {"customerId":"","total":-1}
 *  → 400  application/problem+json  {"detail":"Invalid request content.", ...}
 * </pre>
 * Variações: acrescente {@code --spring.profiles.active=campos} (SB04-12) para listar os campos inválidos.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    public record CreateOrder(@NotBlank String customerId, @Positive BigDecimal total) {}
    public record Order(long id, String customerId, BigDecimal total, String currency) {}

    static class OrderNotFound extends RuntimeException {
        OrderNotFound(long id) { super("Pedido " + id + " não encontrado"); }
    }

    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();
    private final OrdersProperties props;

    public OrderController(OrdersProperties props) { this.props = props; }

    @PostMapping
    ResponseEntity<Order> create(@Valid @RequestBody CreateOrder body) {
        var o = new Order(seq.incrementAndGet(), body.customerId(), body.total(), props.currency());
        store.put(o.id(), o);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(o.id());
        return ResponseEntity.created(location).body(o);          // 201 + Location
    }

    @GetMapping("/{id}")
    Order find(@PathVariable long id) {
        var o = store.get(id);
        if (o == null) throw new OrderNotFound(id);
        return o;
    }

    @GetMapping
    List<Order> list(@RequestParam(defaultValue = "0") BigDecimal min) {
        return store.values().stream().filter(o -> o.total().compareTo(min) >= 0).toList();
    }

    @ExceptionHandler(OrderNotFound.class)
    ProblemDetail notFound(OrderNotFound e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
