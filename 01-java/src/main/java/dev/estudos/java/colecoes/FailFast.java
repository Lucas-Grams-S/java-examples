package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * J07-08 · <em>Fail-fast</em> e {@code ConcurrentModificationException}.
 * Documento: java-docs/01-java/07-colecoes.md §3.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * remover 2 de [1,2,3,4] no for-each -&gt; ConcurrentModificationException
 * remover o PENÚLTIMO (3) de [1,2,3,4] -&gt; terminou sem erro -&gt; [1, 2, 4]
 * removeIf (correto) -&gt; [1, 3]
 * Iterator.remove (correto) -&gt; [1, 3, 4]
 * </pre>
 *
 * <p>Variações: remova o <em>penúltimo</em> elemento de listas de outros tamanhos e observe quando a exceção aparece.
 */
public class FailFast {
    static String removeInForEach(List<Integer> input, int toRemove) {
        var list = new ArrayList<>(input);
        try {
            for (Integer n : list) if (n == toRemove) list.remove(n);
            return "terminou sem erro -> " + list;
        } catch (java.util.ConcurrentModificationException e) {
            return "ConcurrentModificationException";
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("remover 2 de [1,2,3,4] no for-each -> " + removeInForEach(List.of(1, 2, 3, 4), 2));
        out.add("remover o PENÚLTIMO (3) de [1,2,3,4] -> " + removeInForEach(List.of(1, 2, 3, 4), 3));
        var a = new ArrayList<>(List.of(1, 2, 3, 4));
        a.removeIf(n -> n % 2 == 0);
        out.add("removeIf (correto) -> " + a);
        var b = new ArrayList<>(List.of(1, 2, 3, 4));
        for (Iterator<Integer> it = b.iterator(); it.hasNext(); ) if (it.next() == 2) it.remove();
        out.add("Iterator.remove (correto) -> " + b);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
