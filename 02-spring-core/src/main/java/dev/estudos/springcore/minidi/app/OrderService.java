package dev.estudos.springcore.minidi.app;

import dev.estudos.springcore.minidi.Component;

@Component
public class OrderService {
    private final OrderRepository repo;
    private final PaymentGateway gateway;
    OrderService(OrderRepository repo, PaymentGateway gateway) { this.repo = repo; this.gateway = gateway; }
    public String place(String id) { return repo.save(id) + " / " + gateway.charge(1500); }
}
