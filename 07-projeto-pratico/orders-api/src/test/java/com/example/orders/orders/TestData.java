package com.example.orders.orders;

import com.example.orders.catalog.CatalogService;
import com.example.orders.catalog.ProductView;
import com.example.orders.customers.Customer;
import com.example.orders.customers.CustomerRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Fábrica de dados dos testes (e-mails únicos: os testes compartilham o banco do contexto). */
@Component
class TestData {
    @Autowired CustomerRepository customers;
    @Autowired CatalogService catalog;

    Customer customer(String prefix) { return customers.save(new Customer(prefix, prefix + "-" + UUID.randomUUID() + "@x.com")); }
    ProductView product(String price, int stock) { return catalog.create("p-" + UUID.randomUUID(), new BigDecimal(price), stock); }
}
