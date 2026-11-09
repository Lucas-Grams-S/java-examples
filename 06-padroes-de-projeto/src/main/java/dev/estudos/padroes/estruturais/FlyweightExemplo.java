package dev.estudos.padroes.estruturais;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DP03-11 · Flyweight: 100 mil linhas compartilhando 2 regras de imposto; cache de {@code Integer}.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 100.000 linhas criadas -&gt; regras distintas no pool: 2
 * lines[0].rule() == lines[2].rule() -&gt; true
 * lines[0].rule() == lines[1].rule() -&gt; false
 * Integer 127 == 127 -&gt; true; Integer 128 == 128 -&gt; false (sempre use equals)
 * String.intern: new String("x").intern() == "x" -&gt; true
 * </pre>
 *
 * <p>Variações: use {@code new TaxRule(...)} direto no laço e compare com a contagem de instâncias distintas.
 */
public class FlyweightExemplo {

    record TaxRule(String country, String category, int ratePercent) {}

    static final class TaxRuleFactory {
        private static final Map<String, TaxRule> POOL = new HashMap<>();
        static synchronized TaxRule get(String country, String category, int rate) {
            return POOL.computeIfAbsent(country + ":" + category + ":" + rate, k -> new TaxRule(country, category, rate));
        }
        static synchronized int size() { return POOL.size(); }
        static synchronized void clear() { POOL.clear(); }
    }
    record OrderLine(String sku, int qty, TaxRule rule) {}

    @SuppressWarnings("unlikely-arg-type")
    public static List<String> run() {
        TaxRuleFactory.clear();
        List<String> out = new ArrayList<>();
        var lines = new ArrayList<OrderLine>();
        for (int i = 0; i < 100_000; i++) {
            boolean food = i % 2 == 0;
            lines.add(new OrderLine("SKU-" + i, 1, TaxRuleFactory.get("BR", food ? "food" : "other", food ? 7 : 18)));
        }
        out.add("100.000 linhas criadas -> regras distintas no pool: " + TaxRuleFactory.size());
        out.add("lines[0].rule() == lines[2].rule() -> " + (lines.get(0).rule() == lines.get(2).rule()));
        out.add("lines[0].rule() == lines[1].rule() -> " + (lines.get(0).rule() == lines.get(1).rule()));

        Integer a = 127, b = 127, c = 128, d = 128;
        out.add("Integer 127 == 127 -> " + (a == b) + "; Integer 128 == 128 -> " + (c == d) + " (sempre use equals)");
        out.add("String.intern: new String(\"x\").intern() == \"x\" -> " + (new String("x").intern() == "x"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
