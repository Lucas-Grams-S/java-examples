package dev.estudos.outros.modulith.orders;

/** API pública do módulo {@code orders}. */
public record OrderPlaced(String id, String sku, int qty) {}
