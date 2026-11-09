package dev.estudos.boot.testes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class Api {
    private final OrderService orders;
    Api(OrderService orders) { this.orders = orders; }

    @GetMapping("/orders/{id}") public OrderDto get(@PathVariable long id) { return orders.find(id); }

    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    void notFound() {}
}
