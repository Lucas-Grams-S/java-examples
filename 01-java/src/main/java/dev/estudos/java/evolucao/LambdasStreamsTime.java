package dev.estudos.java.evolucao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * J00-03 · Java 8: lambdas, streams e java.time.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes   -&gt; [3, 1]
 * depois  -&gt; [3, 1]
 * vencimento (2026-01-15 + 30 dias) -&gt; 2026-02-14
 * lambda não é classe anônima: true
 * </pre>
 *
 * <p>Variações: troque {@code sorted} por {@code sorted(Comparator.comparing(Order::total).reversed())}.
 */
public class LambdasStreamsTime {
    enum Status { NEW, PAID }
    record Order(int id, Status status, BigDecimal total) {}

    static List<Order> sample() {
        return List.of(new Order(1, Status.PAID, new BigDecimal("30")), new Order(2, Status.NEW, new BigDecimal("5")),
                new Order(3, Status.PAID, new BigDecimal("10")));
    }

    static List<Order> paidOldStyle(List<Order> orders) {
        List<Order> paid = new ArrayList<>();
        for (Order o : orders) if (o.status() == Status.PAID) paid.add(o);
        Collections.sort(paid, new Comparator<Order>() {
            public int compare(Order a, Order b) { return a.total().compareTo(b.total()); }
        });
        return paid;
    }

    static List<Order> paidWithStreams(List<Order> orders) {
        return orders.stream().filter(o -> o.status() == Status.PAID).sorted(Comparator.comparing(Order::total)).toList();
    }

    public static List<String> run() {
        var orders = sample();
        LocalDate due = LocalDate.of(2026, 1, 15).plusDays(30);
        return List.of("antes   -> " + paidOldStyle(orders).stream().map(Order::id).toList(),
                "depois  -> " + paidWithStreams(orders).stream().map(Order::id).toList(),
                "vencimento (2026-01-15 + 30 dias) -> " + due,
                "lambda não é classe anônima: " + (((Comparator<Order>) (a, b) -> 0).getClass().getName().contains("$$Lambda")));
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
