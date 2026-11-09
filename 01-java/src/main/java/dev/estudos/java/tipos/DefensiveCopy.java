package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * J03-12 · Cópia defensiva.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * itens do pedido após mutar a lista original -&gt; 1
 * tentativa de mutar items() -&gt; UnsupportedOperationException
 * </pre>
 *
 * <p>Variações: troque {@code List.copyOf(items)} por {@code this.items = items} e veja o pedido ser alterado de fora.
 */
public class DefensiveCopy {
    public record OrderItem(String sku, int quantity) {}

    public static final class Order {
        private final List<OrderItem> items;

        public Order(List<OrderItem> items) {
            this.items = List.copyOf(items);            // cópia imutável: o chamador não consegue mutar nosso estado
        }
        public List<OrderItem> items() { return items; }   // já imutável: seguro devolver
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<OrderItem> original = new ArrayList<>(List.of(new OrderItem("A", 1)));
        Order order = new Order(original);
        original.add(new OrderItem("B", 2));             // mutação feita pelo chamador depois da construção
        out.add("itens do pedido após mutar a lista original -> " + order.items().size());
        try {
            order.items().add(new OrderItem("C", 3));
        } catch (UnsupportedOperationException e) {
            out.add("tentativa de mutar items() -> UnsupportedOperationException");
        }
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
