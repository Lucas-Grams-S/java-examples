package com.example.orders.customers;

import jakarta.persistence.*;

/** Entidade JPA: fica dentro do módulo; a API só expõe {@link CustomerResponse}. */
@Entity @Table(name = "customers")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    private String email;

    protected Customer() {}
    public Customer(String name, String email) { this.name = name; this.email = email; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
