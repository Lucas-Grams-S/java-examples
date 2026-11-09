package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * J07-11 · Agrupando pedidos por cliente (Java moderno).
 * Documento: java-docs/01-java/07-colecoes.md §5.1 (inclui a imutabilidade de §3.4 como complemento).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * computeIfAbsent -&gt; &#123;A1=[OrderItem[sku=P1, quantity=2], OrderItem[sku=P2, quantity=1]]&#125;
 * getOrDefault de chave ausente -&gt; []
 * merge como contador -&gt; &#123;P1=3, P2=1&#125;
 * unmodifiableList é visão -&gt; [a, b, c]; List.copyOf é cópia -&gt; [a, b]
 * List.copyOf de imutável devolve a mesma -&gt; true
 * List.of com null -&gt; NullPointerException
 * Arrays.asList escreve no array -&gt; 9
 * Arrays.asList.add -&gt; UnsupportedOperationException (tamanho fixo)
 * </pre>
 *
 * <p>Variações: use {@code HashMap.newHashMap(100)} (Java 19+) para dimensionar a capacidade inicial.
 */
public class GroupingOrders {
    record OrderItem(String sku, int quantity) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Map<String, List<OrderItem>> itemsByOrder = new HashMap<>();

        List<OrderItem> list = itemsByOrder.get("A1");   // antigo
        if (list == null) { list = new ArrayList<>(); itemsByOrder.put("A1", list); }
        list.add(new OrderItem("P1", 2));

        itemsByOrder.computeIfAbsent("A1", k -> new ArrayList<>()).add(new OrderItem("P2", 1));   // moderno
        out.add("computeIfAbsent -> " + itemsByOrder);
        out.add("getOrDefault de chave ausente -> " + itemsByOrder.getOrDefault("Z9", List.of()));

        Map<String, Integer> counts = new TreeMap<>();
        for (String sku : List.of("P1", "P2", "P1", "P1")) counts.merge(sku, 1, Integer::sum);
        out.add("merge como contador -> " + counts);

        var original = new ArrayList<>(List.of("a", "b"));
        var view = Collections.unmodifiableList(original);
        var copy = List.copyOf(original);
        original.add("c");
        out.add("unmodifiableList é visão -> " + view + "; List.copyOf é cópia -> " + copy);
        out.add("List.copyOf de imutável devolve a mesma -> " + (List.copyOf(copy) == copy));
        try {
            List.of("a", (String) null);
        } catch (NullPointerException e) {
            out.add("List.of com null -> NullPointerException");
        }
        var arr = new Integer[] {1, 2};
        var fixed = java.util.Arrays.asList(arr);
        fixed.set(0, 9);
        out.add("Arrays.asList escreve no array -> " + arr[0]);
        try {
            fixed.add(3);
        } catch (UnsupportedOperationException e) {
            out.add("Arrays.asList.add -> UnsupportedOperationException (tamanho fixo)");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
