package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collection;

/**
 * J06-05 · PECS: Producer Extends, Consumer Super.
 * Documento: java-docs/01-java/06-generics.md §2.4 e §2.5 (cobre também o conteúdo conceitual de §2.4).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * copyAll(List&lt;Integer&gt; -&gt; List&lt;Number&gt;) -&gt; [1, 2, 3]
 * copyAll(List&lt;Integer&gt; -&gt; List&lt;Object&gt;) -&gt; [1, 2, 3]
 * List&lt;? super Integer&gt;: grava Integer, lê Object -&gt; 7
 * List&lt;?&gt; só lê/mede -&gt; 2
 * readOnly (? extends Number) -&gt; 4.5
 * Comparator&lt;Object&gt; ordena List&lt;String&gt; -&gt; [a, b, c]
 * Collection&lt;? extends Number&gt; -&gt; [1, 2, 3]
 * </pre>
 *
 * <p>Variações: em {@code readOnly}, tente {@code list.add(1)} (não compila; só {@code null}).
 */
public class Pecs {
    public static <T> void copyAll(List<? extends T> src, List<? super T> dest) {
        for (T t : src) dest.add(t);
    }

    @SuppressWarnings("unused")
    static Object readOnly(List<? extends Number> list) {
        // list.add(1);   // não compila
        list.add(null);   // só null é permitido
        return list.isEmpty() ? null : list.get(0);   // lê como Number
    }

    static int countAny(List<?> l) { return l.size(); }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<Integer> ints = List.of(1, 2, 3);
        List<Number> nums = new ArrayList<>();
        copyAll(ints, nums);
        out.add("copyAll(List<Integer> -> List<Number>) -> " + nums);
        List<Object> objs = new ArrayList<>();
        copyAll(ints, objs);
        out.add("copyAll(List<Integer> -> List<Object>) -> " + objs);
        List<Integer> sink = new ArrayList<>();
        List<? super Integer> consumer = sink;
        consumer.add(7);
        Object lido = consumer.get(0);   // só lê como Object
        out.add("List<? super Integer>: grava Integer, lê Object -> " + lido);
        out.add("List<?> só lê/mede -> " + countAny(List.of("a", "b")));
        out.add("readOnly (? extends Number) -> " + readOnly(new ArrayList<>(List.of(4.5))));
        Comparator<Object> byString = Comparator.comparing(Object::toString);
        List<String> words = new ArrayList<>(List.of("b", "c", "a"));
        words.sort(byString);   // Comparator<Object> ordena List<String> (Comparator<? super T>)
        out.add("Comparator<Object> ordena List<String> -> " + words);
        Collection<? extends Number> c = ints;
        out.add("Collection<? extends Number> -> " + c);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
