package com.example.orders.customers;

public record CustomerResponse(Long id, String name, String email) {
    static CustomerResponse of(Customer c) { return new CustomerResponse(c.getId(), c.getName(), c.getEmail()); }
}
