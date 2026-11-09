package dev.estudos.boot.testes;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository repo;
    OrderService(OrderRepository repo) { this.repo = repo; }

    public OrderDto find(long id) {
        return repo.findById(id).map(o -> new OrderDto(o.getId(), o.getCustomer()))
                   .orElseThrow(() -> new IllegalArgumentException("pedido " + id));
    }
}
