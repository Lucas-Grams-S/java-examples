package com.example.orders.orders;

import com.example.orders.shared.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** PP0: raiz do agregado em Java puro (sem Spring, sem JPA). Imutável: {@link #cancel()} devolve um novo pedido. */
public final class Order {
    private final Long id;
    private final Long customerId;
    private final List<OrderItem> items;
    private final OrderStatus status;
    private final Instant createdAt;

    private Order(Long id, Long customerId, List<OrderItem> items, OrderStatus status, Instant createdAt) {
        if (items == null || items.isEmpty()) throw new DomainException("order must have items");
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);   // cópia imutável: quem chamou não consegue alterar o agregado por fora
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Order create(Long customerId, List<OrderItem> items, Instant now) {
        return new Order(null, customerId, items, OrderStatus.NEW, now);
    }

    public static Order restore(Long id, Long customerId, List<OrderItem> items, OrderStatus status, Instant createdAt) {
        return new Order(id, customerId, items, status, createdAt);
    }

    /** Só se cancela pedido que ainda não foi enviado (nem já cancelado). */
    public CancelResult cancel() {
        return switch (status) {
            case SHIPPED -> new CancelResult.Rejected("shipped order cannot be canceled");
            case CANCELED -> new CancelResult.Rejected("order already canceled");
            case NEW, PAID -> new CancelResult.Canceled(new Order(id, customerId, items, OrderStatus.CANCELED, createdAt));
        };
    }

    public BigDecimal total() {
        return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long id() { return id; }
    public Long customerId() { return customerId; }
    public List<OrderItem> items() { return items; }
    public OrderStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
}
