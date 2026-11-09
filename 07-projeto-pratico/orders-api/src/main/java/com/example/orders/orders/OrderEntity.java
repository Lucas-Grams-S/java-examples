package com.example.orders.orders;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Modelo de persistência separado do domínio: a entidade JPA nunca sai da camada. */
@Entity @Table(name = "orders")
class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long customerId;
    @Enumerated(EnumType.STRING) private OrderStatus status;
    private Instant createdAt;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {}
    OrderEntity(Order o) {
        this.customerId = o.customerId(); this.status = o.status(); this.createdAt = o.createdAt();
        o.items().forEach(i -> items.add(new OrderItemEntity(i)));
    }
    Order toDomain() {
        return Order.restore(id, customerId, items.stream().map(OrderItemEntity::toDomain).toList(), status, createdAt);
    }
    Long getId() { return id; }
    Long getCustomerId() { return customerId; }
    OrderStatus getStatus() { return status; }
    void setStatus(OrderStatus s) { this.status = s; }
    Instant getCreatedAt() { return createdAt; }
    List<OrderItemEntity> getItems() { return items; }
}
