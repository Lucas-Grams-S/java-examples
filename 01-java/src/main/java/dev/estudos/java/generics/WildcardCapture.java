package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * J06-15 · Captura de wildcard.
 * Documento: java-docs/01-java/06-generics.md §5.4 (e a inferência de §5.5 como variação).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * swapFirstTwo -&gt; [b, a, c]
 * emptyList inferido -&gt; []
 * type witness -&gt; []
 * </pre>
 *
 * <p>Variações: tente escrever o corpo de {@code swapHelper} direto em {@code swapFirstTwo} (não compila: capture of ?).
 */
public class WildcardCapture {
    static void swapFirstTwo(List<?> list) { swapHelper(list); }

    private static <T> void swapHelper(List<T> list) {
        T tmp = list.get(0);
        list.set(0, list.get(1));
        list.set(1, tmp);
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<String> l = new ArrayList<>(List.of("a", "b", "c"));
        swapFirstTwo(l);
        out.add("swapFirstTwo -> " + l);
        List<String> empty = java.util.Collections.emptyList();   // T inferido pelo tipo-alvo
        out.add("emptyList inferido -> " + empty);
        out.add("type witness -> " + java.util.Collections.<Integer>emptyList());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
