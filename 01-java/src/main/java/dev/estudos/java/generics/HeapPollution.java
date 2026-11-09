package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * J06-11 · Heap pollution e raw types.
 * Documento: java-docs/01-java/06-generics.md §3.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * depois de raw.add(42), strings.size() -&gt; 1
 * strings.get(0) -&gt; ClassCastException (longe da causa)
 * &#64;SafeVarargs listOf -&gt; [a, b]
 * </pre>
 *
 * <p>Variações: compile com {@code javac -Xlint:unchecked} e veja os avisos que o {@code @SuppressWarnings} está calando aqui.
 */
public class HeapPollution {
    @SafeVarargs
    static <T> List<T> listOf(T... xs) { return new ArrayList<>(List.of(xs)); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        List raw = strings;                      // aviso unchecked
        raw.add(42);                             // compila; polui o heap
        out.add("depois de raw.add(42), strings.size() -> " + strings.size());
        try {
            String s = strings.get(0);           // cast inserido pelo compilador
            out.add("sem erro " + s);
        } catch (ClassCastException e) {
            out.add("strings.get(0) -> ClassCastException (longe da causa)");
        }
        out.add("@SafeVarargs listOf -> " + listOf("a", "b"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
