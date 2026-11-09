package dev.estudos.springcore.contexto.app;

import org.springframework.stereotype.Service;

@Service
public class OrderSvc {
    private final OrderRepo repo;
    public OrderSvc(OrderRepo repo) { this.repo = repo; }
    public OrderRepo repo() { return repo; }
}
