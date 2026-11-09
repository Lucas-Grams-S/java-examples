package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-12 · flatMap: Stream de pedidos para Stream de itens; mapMulti como alternativa.
 * Documento: java-docs/01-java/09-programacao-funcional.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * itens -&gt; [caneta, caderno, lápis, mochila, caneta]
 * com map (lista de listas) -&gt; [2, 1, 1, 0, 1]
 * mapMulti -&gt; [caneta, caderno, lápis, mochila, caneta]
 * total de unidades -&gt; 10
 * </pre>
 *
 * <p>Variações: troque flatMap por map e observe Stream<List<OrderItem>>.
 */
public class FlatMapDemo {
    public static List<String> run() {
        List<Order> orders = Domain.orders();
        List<String> out = new ArrayList<>();
        List<OrderItem> all = orders.stream().flatMap(o -> o.items().stream()).toList();
        out.add("itens -> " + all.stream().map(OrderItem::sku).toList());
        out.add("com map (lista de listas) -> " + orders.stream().map(Order::items).map(List::size).toList());
        List<String> multi = orders.stream().<String>mapMulti((o, sink) -> o.items().forEach(i -> sink.accept(i.sku()))).toList();
        out.add("mapMulti -> " + multi);
        out.add("total de unidades -> " + all.stream().mapToInt(OrderItem::qty).sum());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
