package dev.estudos.outros.graphql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class OrderGraphQl {
    public record Order(String id, String status) {}
    public record Customer(String id, String name) {}

    /** Quantas vezes o método em lote foi chamado e com quantos pedidos (para provar que não há N+1). */
    public static final List<Integer> LOTES = new CopyOnWriteArrayList<>();

    @QueryMapping
    Order orderById(@Argument String id) { return new Order(id, "PAID"); }

    @QueryMapping
    List<Order> orders() { return List.of(new Order("1", "PAID"), new Order("2", "NEW"), new Order("3", "SHIPPED")); }

    /** Um DataLoader agrupa as chaves: uma chamada para todos os pedidos da lista. */
    @BatchMapping
    Map<Order, Customer> customer(List<Order> orders) {
        LOTES.add(orders.size());
        Map<Order, Customer> m = new HashMap<>();
        for (Order o : new ArrayList<>(orders)) m.put(o, new Customer("c" + o.id(), "Cliente " + o.id()));
        return m;
    }
}
