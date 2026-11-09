package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * J05-13 · DTO com validação e cópia defensiva.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * lista original mutada -&gt; 2 itens; no record -&gt; 1
 * lista do record é imutável -&gt; UnsupportedOperationException
 * sem itens -&gt; pedido sem itens
 * customerId nulo -&gt; NullPointerException
 * </pre>
 *
 * <p>Variações: remova {@code List.copyOf} e mute a lista original depois de criar o record.
 */
public class DtoValidation {
    public record OrderItem(String sku, int quantity) {}

    public record CreateOrderRequest(UUID customerId, List<OrderItem> items) {
        public CreateOrderRequest {
            Objects.requireNonNull(customerId);
            items = List.copyOf(items);   // atribui ao parâmetro: o campo recebe a cópia
            if (items.isEmpty()) throw new IllegalArgumentException("pedido sem itens");
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var original = new ArrayList<>(List.of(new OrderItem("A", 1)));
        var req = new CreateOrderRequest(new UUID(0, 7), original);
        original.add(new OrderItem("B", 2));
        out.add("lista original mutada -> " + original.size() + " itens; no record -> " + req.items().size());
        try {
            req.items().add(new OrderItem("C", 1));
        } catch (UnsupportedOperationException e) {
            out.add("lista do record é imutável -> UnsupportedOperationException");
        }
        try {
            new CreateOrderRequest(new UUID(0, 7), List.of());
        } catch (IllegalArgumentException e) {
            out.add("sem itens -> " + e.getMessage());
        }
        try {
            new CreateOrderRequest(null, List.of(new OrderItem("A", 1)));
        } catch (NullPointerException e) {
            out.add("customerId nulo -> NullPointerException");
        }
        return out;
    }


    public static void main(String[] args) { run().forEach(System.out::println); }
}
