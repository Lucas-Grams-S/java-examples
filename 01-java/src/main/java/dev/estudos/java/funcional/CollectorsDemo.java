package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-05 · Collectors: groupingBy, mapping, partitioningBy, toMap com merge e teeing.
 * Documento: java-docs/01-java/09-programacao-funcional.md §2.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * contagem -&gt; &#123;Ana=2, Bia=2, Caio=1&#125;
 * ids -&gt; &#123;Ana=[1, 3], Bia=[2, 5], Caio=[4]&#125;
 * pago? -&gt; &#123;false=2, true=3&#125;
 * total por cliente -&gt; &#123;Ana=370, Bia=75, Caio=80&#125;
 * toMap sem merge -&gt; Duplicate key Ana (attempted merging values 120 and 250)
 * média (teeing) -&gt; 105
 * </pre>
 *
 * <p>Variações: remova o merge do toMap e veja o IllegalStateException de chave duplicada.
 */
public class CollectorsDemo {
    public static List<String> run() {
        List<Order> orders = Domain.orders();
        List<String> out = new ArrayList<>();
        Map<String, Long> countByCustomer = orders.stream()
            .collect(Collectors.groupingBy(o -> o.customer().name(), TreeMap::new, Collectors.counting()));
        out.add("contagem -> " + countByCustomer);
        Map<String, List<Long>> ids = orders.stream()
            .collect(Collectors.groupingBy(o -> o.customer().name(), TreeMap::new,
                     Collectors.mapping(Order::id, Collectors.toList())));
        out.add("ids -> " + ids);
        Map<Boolean, Long> parts = orders.stream()
            .collect(Collectors.partitioningBy(o -> o.status() == OrderStatus.PAID, Collectors.counting()));
        out.add("pago? -> " + parts);
        Map<String, BigDecimal> total = orders.stream()
            .collect(Collectors.toMap(o -> o.customer().name(), Order::total, BigDecimal::add, TreeMap::new));
        out.add("total por cliente -> " + total);
        try {
            orders.stream().collect(Collectors.toMap(o -> o.customer().name(), Order::total));
        } catch (IllegalStateException e) { out.add("toMap sem merge -> " + e.getMessage()); }
        long media = orders.stream().collect(Collectors.teeing(
            Collectors.counting(), Collectors.summingInt(o -> o.total().intValue()), (n, sum) -> sum / n));
        out.add("média (teeing) -> " + media);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
