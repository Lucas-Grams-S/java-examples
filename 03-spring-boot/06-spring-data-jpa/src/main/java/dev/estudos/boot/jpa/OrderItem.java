package dev.estudos.boot.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;
    private String sku;
    private int qty;
    private BigDecimal price;

    protected OrderItem() {}
    OrderItem(Order order, String sku, int qty, BigDecimal price) {
        this.order = order; this.sku = sku; this.qty = qty; this.price = price;
    }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public BigDecimal getPrice() { return price; }
}
