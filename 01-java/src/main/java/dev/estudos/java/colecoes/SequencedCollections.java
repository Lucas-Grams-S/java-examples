package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * J07-05 · Sequenced Collections (Java 21, JEP 431).
 * Documento: java-docs/01-java/07-colecoes.md §2.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * getFirst/getLast -&gt; 1/3
 * reversed() -&gt; [3, 2, 1]
 * reversed() é visão: após list.add(4) -&gt; [4, 3, 2, 1]
 * LinkedHashSet.reversed() -&gt; [c, a, b]; getLast -&gt; c
 * lastEntry -&gt; y=2
 * putFirst(z, 9) -&gt; &#123;z=9, x=1, y=2&#125;
 * map.reversed() -&gt; &#123;y=2, x=1, z=9&#125;
 * List.of(...).addFirst -&gt; UnsupportedOperationException
 * </pre>
 *
 * <p>Variações: troque {@code LinkedHashSet} por {@code HashSet} (não implementa {@code SequencedCollection}).
 */
public class SequencedCollections {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var list = new ArrayList<>(List.of(1, 2, 3));
        out.add("getFirst/getLast -> " + list.getFirst() + "/" + list.getLast());
        var rev = list.reversed();
        out.add("reversed() -> " + rev);
        list.add(4);
        out.add("reversed() é visão: após list.add(4) -> " + rev);

        var set = new LinkedHashSet<>(List.of("b", "a", "c"));
        out.add("LinkedHashSet.reversed() -> " + set.reversed() + "; getLast -> " + set.getLast());

        var map = new LinkedHashMap<String, Integer>();
        map.put("x", 1);
        map.put("y", 2);
        out.add("lastEntry -> " + map.lastEntry());
        map.putFirst("z", 9);
        out.add("putFirst(z, 9) -> " + map);
        out.add("map.reversed() -> " + map.reversed());

        try {
            List.of(1).addFirst(0);
        } catch (UnsupportedOperationException e) {
            out.add("List.of(...).addFirst -> UnsupportedOperationException");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
