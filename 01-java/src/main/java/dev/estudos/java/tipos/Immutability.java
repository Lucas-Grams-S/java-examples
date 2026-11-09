package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * J03-07 · Imutabilidade como estratégia.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.7.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * item válido -&gt; OrderItem[sku=A1, quantity=2]
 * quantidade 0 -&gt; quantity must be &gt; 0
 * final List -&gt; [ainda aceita add]
 * List.of(...).add -&gt; UnsupportedOperationException
 * </pre>
 */
public class Immutability {
    public record OrderItem(String sku, int quantity) {
        public OrderItem {                                   // construtor compacto: validação
            if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("item válido -> " + new OrderItem("A1", 2));
        try {
            new OrderItem("A1", 0);
        } catch (IllegalArgumentException e) {
            out.add("quantidade 0 -> " + e.getMessage());
        }
        final List<String> l = new ArrayList<>();            // final na variável não torna o objeto imutável
        l.add("ainda aceita add");
        out.add("final List -> " + l);
        try {
            List.of("a").add("b");
        } catch (UnsupportedOperationException e) {
            out.add("List.of(...).add -> UnsupportedOperationException");
        }
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
