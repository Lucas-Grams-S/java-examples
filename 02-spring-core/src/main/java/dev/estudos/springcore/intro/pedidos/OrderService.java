package dev.estudos.springcore.intro.pedidos;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository repo;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository repo, PaymentGateway gateway) { // injetado pelo contêiner (ou à mão)
        this.repo = repo;
        this.gateway = gateway;
    }

    public String place(Order order) {
        String receipt = gateway.charge(order);
        repo.save(order);
        return receipt;
    }

    public int count() { return repo.findAll().size(); }
}
