package com.example.orders.orders;

import com.example.orders.orders.OrderRequests.CreateOrderRequest;
import com.example.orders.orders.OrderRequests.OrderResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** PP2: controller fino, só traduz HTTP <-> serviço. */
@RestController
@RequestMapping("/orders")
class OrderController {
    private final OrderService service;
    OrderController(OrderService service) { this.service = service; }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        var order = service.create(request);
        return ResponseEntity.created(URI.create("/orders/" + order.id())).body(OrderResponse.of(order));
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) { return OrderResponse.of(service.find(id)); }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable Long id) { return OrderResponse.of(service.cancel(id)); }
}
