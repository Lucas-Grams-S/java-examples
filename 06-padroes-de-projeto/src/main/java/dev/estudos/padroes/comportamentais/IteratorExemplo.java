package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * DP04-08 · Iterator: {@code Iterable} próprio, fail-fast e remoção segura pelo iterador.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * soma de Range(1,5) com for-each -&gt; 10
 * remover no for-each -&gt; ConcurrentModificationException
 * remover pelo iterador -&gt; [1, 3]
 * </pre>
 *
 * <p>Variações: troque {@code ArrayList} por {@code CopyOnWriteArrayList} e veja o for-each deixar de lançar.
 */
public class IteratorExemplo {

    static class Range implements Iterable<Integer> {
        final int lo, hi;
        Range(int lo, int hi) { this.lo = lo; this.hi = hi; }
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                int cur = lo;
                public boolean hasNext() { return cur < hi; }
                public Integer next() { if (!hasNext()) throw new NoSuchElementException(); return cur++; }
            };
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        int sum = 0;
        for (int x : new Range(1, 5)) sum += x;
        out.add("soma de Range(1,5) com for-each -> " + sum);

        var list = new ArrayList<>(List.of(1, 2, 3));
        try {
            for (var x : list) if (x == 1) list.remove(x);
            out.add("remover no for-each -> sem erro?!");
        } catch (ConcurrentModificationException e) {
            out.add("remover no for-each -> ConcurrentModificationException");
        }
        var list2 = new ArrayList<>(List.of(1, 2, 3, 4));
        var it = list2.iterator();
        while (it.hasNext()) if (it.next() % 2 == 0) it.remove();
        out.add("remover pelo iterador -> " + list2);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
