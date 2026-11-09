package dev.estudos.springcore.intro.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();
    public void save(Order order) { orders.add(order); }
    public List<Order> findAll() { return List.copyOf(orders); }
}
