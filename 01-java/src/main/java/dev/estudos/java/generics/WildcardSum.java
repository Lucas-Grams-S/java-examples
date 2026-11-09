package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * J06-13 · Soma com wildcard de produtor.
 * Documento: java-docs/01-java/06-generics.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * total(List&lt;Integer&gt;) -&gt; 6.0
 * total(List&lt;Double&gt;) -&gt; 4.0
 * </pre>
 *
 * <p>Variações: troque o parâmetro por {@code Collection<Number>} e veja {@code total(List.of(1, 2, 3))} deixar de compilar.
 */
public class WildcardSum {
    static double total(Collection<? extends Number> values) {
        double sum = 0;
        for (Number n : values) sum += n.doubleValue();
        return sum;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("total(List<Integer>) -> " + total(List.of(1, 2, 3)));
        out.add("total(List<Double>) -> " + total(List.of(1.5, 2.5)));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
