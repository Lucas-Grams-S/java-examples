package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-11 · Relatório de pedidos: ranking, estatísticas e junção de nomes.
 * Documento: java-docs/01-java/09-programacao-funcional.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * ranking -&gt; [3:250, 1:120, 4:80, 5:45]
 * stats -&gt; IntSummaryStatistics&#123;count=5, sum=525, min=30, average=105.000000, max=250&#125;
 * clientes -&gt; [Ana, Bia, Caio]
 * toList() é imutável
 * </pre>
 *
 * <p>Variações: inclua CREATED no filtro e veja o ranking mudar.
 */
public class OrderReport {
    public static List<String> run() {
        List<Order> orders = Domain.orders();
        List<String> out = new ArrayList<>();
        List<Order> ranking = orders.stream()
            .filter(o -> o.status() != OrderStatus.CREATED)
            .sorted(Comparator.comparing(Order::total).reversed())
            .limit(10)
            .toList();
        out.add("ranking -> " + ranking.stream().map(o -> o.id() + ":" + o.total()).toList());
        IntSummaryStatistics stats = orders.stream().mapToInt(o -> o.total().intValue()).summaryStatistics();
        out.add("stats -> " + stats);
        out.add("clientes -> " + orders.stream().map(o -> o.customer().name()).distinct()
                .collect(Collectors.joining(", ", "[", "]")));
        try { ranking.add(orders.get(0)); } catch (UnsupportedOperationException e) { out.add("toList() é imutável"); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
