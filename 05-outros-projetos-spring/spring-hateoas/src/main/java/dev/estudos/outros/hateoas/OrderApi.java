package dev.estudos.outros.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderApi {
    public record Order(String id, String status) {}

    @GetMapping("/orders/{id}")
    EntityModel<Order> get(@PathVariable String id) {
        return EntityModel.of(new Order(id, "PAID"),
                linkTo(methodOn(OrderApi.class).get(id)).withSelfRel(),
                linkTo(methodOn(OrderApi.class).pay(id)).withRel("pay"));
    }

    @PostMapping("/orders/{id}/pay")
    ResponseEntity<Void> pay(@PathVariable String id) { return null; }   // exemplo mínimo
}
