package com.example.ordersvc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

/** HTTP Service Client (Framework 7) para o catalog. */
interface CatalogClient {
    record Product(long id, String name, long priceCents, int stock, String currency) {}

    @GetExchange("/products/{id}")
    Product product(@PathVariable long id);
}
