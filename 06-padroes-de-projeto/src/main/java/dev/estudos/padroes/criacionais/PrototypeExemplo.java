package dev.estudos.padroes.criacionais;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * DP02-10 · Prototype: a armadilha de {@code clone()} raso, construtor de cópia e escopo {@code prototype}.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * clone raso: d1.skus -&gt; [A, B] (alterado pelo clone), mesma lista -&gt; true
 * construtor de cópia: o1 -&gt; ana[A, X], o2 -&gt; ana[A, X, Y]
 * with-er: bia[A, X]
 * escopo prototype: instâncias distintas -&gt; true
 * </pre>
 *
 * <p>Variações: troque {@code new ArrayList<>(skus)} por {@code skus} no construtor e veja a cópia deixar de ser profunda.
 */
public class PrototypeExemplo {

    static class Draft implements Cloneable {
        String customer;
        List<String> skus = new ArrayList<>();
        Draft(String customer) { this.customer = customer; }
        @Override public Draft clone() {
            try { return (Draft) super.clone(); } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
    }

    static class OrderDraft {
        private final String customer;
        private final List<String> skus;
        OrderDraft(String customer, List<String> skus) { this.customer = customer; this.skus = new ArrayList<>(skus); }
        OrderDraft(OrderDraft other) { this(other.customer, other.skus); }
        OrderDraft withCustomer(String c) { return new OrderDraft(c, skus); }
        void add(String sku) { skus.add(sku); }
        @Override public String toString() { return customer + skus; }
    }

    static class ShoppingCart { final List<String> items = new ArrayList<>(); }
    @Configuration static class CartConfig { @Bean @Scope("prototype") ShoppingCart cart() { return new ShoppingCart(); } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var d1 = new Draft("ana"); d1.skus.add("A");
        var d2 = d1.clone(); d2.skus.add("B");
        out.add("clone raso: d1.skus -> " + d1.skus + " (alterado pelo clone), mesma lista -> " + (d1.skus == d2.skus));

        var o1 = new OrderDraft("ana", List.of("A", "X"));
        var o2 = new OrderDraft(o1); o2.add("Y");
        out.add("construtor de cópia: o1 -> " + o1 + ", o2 -> " + o2);
        out.add("with-er: " + o1.withCustomer("bia"));

        try (var ctx = new AnnotationConfigApplicationContext(CartConfig.class)) {
            out.add("escopo prototype: instâncias distintas -> " + (ctx.getBean(ShoppingCart.class) != ctx.getBean(ShoppingCart.class)));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
