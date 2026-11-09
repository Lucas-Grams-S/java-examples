package com.example.orders.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    private BigDecimal price;   // nunca double para dinheiro
    private int stock;

    protected Product() {}
    public Product(String name, BigDecimal price, int stock) { this.name = name; this.price = price; this.stock = stock; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
}
