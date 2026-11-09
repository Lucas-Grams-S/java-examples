package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.List;

/** Domínio comum dos exemplos J09 (não é um exemplo). */
final class Domain {
    private Domain() {}
    enum OrderStatus { CREATED, PAID, SHIPPED }
    record Customer(String name, String city) {}
    record OrderItem(String sku, int qty) {}
    record Order(long id, Customer customer, OrderStatus status, BigDecimal total, List<OrderItem> items) {}

    static final Customer ANA = new Customer("Ana", "Recife");
    static final Customer BIA = new Customer("Bia", "");
    static final Customer CAIO = new Customer("Caio", "Natal");

    static List<Order> orders() {
        return List.of(
            new Order(1, ANA, OrderStatus.PAID, new BigDecimal("120"), List.of(new OrderItem("caneta", 2), new OrderItem("caderno", 1))),
            new Order(2, BIA, OrderStatus.CREATED, new BigDecimal("30"), List.of(new OrderItem("lápis", 5))),
            new Order(3, ANA, OrderStatus.SHIPPED, new BigDecimal("250"), List.of(new OrderItem("mochila", 1))),
            new Order(4, CAIO, OrderStatus.PAID, new BigDecimal("80"), List.of()),
            new Order(5, BIA, OrderStatus.PAID, new BigDecimal("45"), List.of(new OrderItem("caneta", 1))));
    }
}
