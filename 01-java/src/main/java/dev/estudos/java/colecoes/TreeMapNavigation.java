package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * J07-12 · Navegação com {@code TreeMap}.
 * Documento: java-docs/01-java/07-colecoes.md §5.2 (inclui o erro de comparador inconsistente de §8).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * floorKey(6) -&gt; 5
 * ceilingKey(6) -&gt; 9
 * headMap(5) -&gt; &#123;1=a&#125;
 * tailMap(5, true) -&gt; &#123;5=e, 9=i&#125;
 * firstEntry/lastEntry -&gt; 1=a / 9=i
 * frete para 7 kg (floorEntry) -&gt; R$ 20
 * TreeSet com comparing(sku): 2 produtos de mesmo SKU -&gt; tamanho 1
 * TreeMap com chave null -&gt; NullPointerException
 * </pre>
 *
 * <p>Variações: monte uma tabela de frete por faixa de peso com {@code floorEntry}.
 */
public class TreeMapNavigation {
    record Product(String sku, String name) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var tm = new TreeMap<>(Map.of(1, "a", 5, "e", 9, "i"));
        out.add("floorKey(6) -> " + tm.floorKey(6));
        out.add("ceilingKey(6) -> " + tm.ceilingKey(6));
        out.add("headMap(5) -> " + tm.headMap(5));
        out.add("tailMap(5, true) -> " + tm.tailMap(5, true));
        out.add("firstEntry/lastEntry -> " + tm.firstEntry() + " / " + tm.lastEntry());

        var frete = new TreeMap<>(Map.of(0, "R$ 10", 5, "R$ 20", 20, "R$ 50"));
        out.add("frete para 7 kg (floorEntry) -> " + frete.floorEntry(7).getValue());

        var byName = new TreeSet<>(Comparator.comparing(Product::sku));
        byName.add(new Product("X", "Caneta"));
        byName.add(new Product("X", "Lápis"));
        out.add("TreeSet com comparing(sku): 2 produtos de mesmo SKU -> tamanho " + byName.size());
        try {
            new TreeMap<String, Integer>().put(null, 1);
        } catch (NullPointerException e) {
            out.add("TreeMap com chave null -> NullPointerException");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
