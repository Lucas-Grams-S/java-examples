package dev.estudos.boot.dados;

import org.springframework.data.relational.core.mapping.Table;

@Table("ORDER_ITEM")
public record OrderItem(String sku, int quantity) {}
