package com.example.orders.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "order_items")
class OrderItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;

    protected OrderItemEntity() {}
    OrderItemEntity(OrderItem i) { this.productId = i.productId(); this.quantity = i.quantity(); this.unitPrice = i.unitPrice(); }
    OrderItem toDomain() { return new OrderItem(productId, quantity, unitPrice); }
}
