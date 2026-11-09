package com.example.orders.catalog;

import java.math.BigDecimal;

/** Visão imutável (é isto que fica no cache, não a entidade). */
public record ProductView(Long id, String name, BigDecimal price) {}
