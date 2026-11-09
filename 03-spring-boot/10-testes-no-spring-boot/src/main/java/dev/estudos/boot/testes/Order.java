package dev.estudos.boot.testes;

import jakarta.persistence.*;

@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String customer;
    protected Order() {}
    public Order(String customer) { this.customer = customer; }
    public Long getId() { return id; }
    public String getCustomer() { return customer; }
}
