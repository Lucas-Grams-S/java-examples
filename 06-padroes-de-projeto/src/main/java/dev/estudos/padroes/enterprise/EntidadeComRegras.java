package dev.estudos.padroes.enterprise;

import dev.estudos.padroes.enterprise.DominioPedidos.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DP05-10 · Entidade com regras e Repository como porta (Java puro).
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * total = 20.00
 * pay vazio -&gt; pedido vazio
 * items() é cópia imutável -&gt; UnsupportedOperationException
 * status após pay = PAID
 * findById(99) vazio = true
 * </pre>
 */
public class EntidadeComRegras {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var repo = new InMemoryOrderRepository();
        var o = new Order(1, new Customer(1, "Ana", true));
        o.add(new OrderItem("A", 2, new BigDecimal("10.00")));
        repo.save(o);
        out.add("total = " + repo.findById(1).orElseThrow().total());
        try { new Order(2, new Customer(2, "Bob", false)).pay(); }
        catch (IllegalStateException e) { out.add("pay vazio -> " + e.getMessage()); }
        try { o.items().add(new OrderItem("X", 1, BigDecimal.ONE)); }
        catch (UnsupportedOperationException e) { out.add("items() é cópia imutável -> UnsupportedOperationException"); }
        o.pay();
        out.add("status após pay = " + o.status());
        out.add("findById(99) vazio = " + repo.findById(99).isEmpty());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
