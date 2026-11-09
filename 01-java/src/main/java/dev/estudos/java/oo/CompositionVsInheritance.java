package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * J04-08 · Composição vs herança.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.8.
 *
 * <p>O documento usa {@code ArrayList}, mas no JDK 25 {@code ArrayList.addAll} <em>não</em> chama {@code add()}, então o
 * bug não aparece (ver DIVERGENCIAS.md). Com {@code HashSet}, cujo {@code addAll} (de {@code AbstractCollection}) chama
 * {@code add()}, o bug é real: a contagem dobra.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * herança HashSet: addAll de 3 -&gt; adds=6 (esperado 3)
 * herança ArrayList no JDK 25: addAll de 3 -&gt; adds=3
 * composição: addAll de 3 -&gt; adds=3, size=3
 * injeção troca a implementação -&gt; pix 10 | cartão 10
 * </pre>
 *
 * <p>Variações: troque {@code HashSet} por {@code ArrayList} em {@code CountingSet} e compare.
 */
public class CompositionVsInheritance {
    static class CountingSet<E> extends HashSet<E> {
        int adds;
        @Override public boolean add(E e) { adds++; return super.add(e); }
        @Override public boolean addAll(Collection<? extends E> c) { adds += c.size(); return super.addAll(c); }
    }

    static class CountingList<E> extends ArrayList<E> {
        int adds;
        @Override public boolean add(E e) { adds++; return super.add(e); }
        @Override public boolean addAll(Collection<? extends E> c) { adds += c.size(); return super.addAll(c); }
    }

    /** Composição: tem-um Set e delega; a contagem não depende da implementação interna. */
    static class CountingCollection<E> {
        private final Set<E> delegate = new HashSet<>();
        int adds;
        boolean add(E e) { adds++; return delegate.add(e); }
        boolean addAll(Collection<? extends E> c) { boolean ch = false; for (E e : c) ch |= add(e); return ch; }
        int size() { return delegate.size(); }
    }

    interface PaymentGateway { String charge(int amount); }

    static class OrderService {
        private final PaymentGateway gateway;
        OrderService(PaymentGateway gateway) { this.gateway = gateway; }
        String checkout(int total) { return gateway.charge(total); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var cs = new CountingSet<String>();
        cs.addAll(List.of("a", "b", "c"));
        out.add("herança HashSet: addAll de 3 -> adds=" + cs.adds + " (esperado 3)");
        var cl = new CountingList<String>();
        cl.addAll(List.of("a", "b", "c"));
        out.add("herança ArrayList no JDK 25: addAll de 3 -> adds=" + cl.adds);
        var cc = new CountingCollection<String>();
        cc.addAll(List.of("a", "b", "c"));
        out.add("composição: addAll de 3 -> adds=" + cc.adds + ", size=" + cc.size());
        out.add("injeção troca a implementação -> "
                + new OrderService(a -> "pix " + a).checkout(10) + " | " + new OrderService(a -> "cartão " + a).checkout(10));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
