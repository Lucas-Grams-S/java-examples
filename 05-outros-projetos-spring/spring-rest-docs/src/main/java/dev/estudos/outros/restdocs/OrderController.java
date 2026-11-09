package dev.estudos.outros.restdocs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    public record Order(String id, String status) {}

    @GetMapping("/orders/{id}")
    Order get(@PathVariable String id) { return new Order(id, "PAID"); }
}
