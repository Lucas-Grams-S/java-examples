package dev.estudos.padroes.enterprise;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domínio compartilhado de DP05-10..14 (infraestrutura, não é um exemplo): entidade com regras,
 * porta {@link OrderRepository} e adaptador em memória.
 */
public final class DominioPedidos {
    private DominioPedidos() {}

    public record Customer(long id, String name, boolean vip) {}

    public record OrderItem(String sku, int qty, BigDecimal price) {
        public BigDecimal subtotal() { return price.multiply(BigDecimal.valueOf(qty)); }
    }

    public static final class Order {
        private final long id;
        private final Customer customer;
        private final List<OrderItem> items = new ArrayList<>();
        private String status = "NEW";

        public Order(long id, Customer c) { this.id = id; this.customer = c; }
        public long id() { return id; }
        public Customer customer() { return customer; }
        public String status() { return status; }
        public List<OrderItem> items() { return List.copyOf(items); }   // cópia defensiva
        public void add(OrderItem i) { items.add(i); }
        public void pay() {                                              // a regra mora no domínio
            if (items.isEmpty()) throw new IllegalStateException("pedido vazio");
            status = "PAID";
        }
        public BigDecimal total() {
            return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /** Porta definida pelo domínio: sem JPA, sem Spring. */
    public interface OrderRepository {
        Optional<Order> findById(long id);
        List<Order> findAll();
        void save(Order o);
    }

    /** Adaptador para testes. */
    public static class InMemoryOrderRepository implements OrderRepository {
        final Map<Long, Order> db = new LinkedHashMap<>();
        public Optional<Order> findById(long id) { return Optional.ofNullable(db.get(id)); }
        public List<Order> findAll() { return List.copyOf(db.values()); }
        public void save(Order o) { db.put(o.id(), o); }
    }
}
