package dev.estudos.boot.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** SB06-01: entidade com relacionamentos, cascade e @Version. */
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal total = BigDecimal.ZERO;

    @Version
    private Long version;

    protected Order() {}
    public Order(Customer c) { this.customer = c; }

    /** Mantém os dois lados sincronizados e o total. */
    public void add(String sku, int qty, BigDecimal price) {
        items.add(new OrderItem(this, sku, qty, price));
        total = total.add(price.multiply(BigDecimal.valueOf(qty)));
    }
    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { this.total = t; }
    public Long getVersion() { return version; }
}
