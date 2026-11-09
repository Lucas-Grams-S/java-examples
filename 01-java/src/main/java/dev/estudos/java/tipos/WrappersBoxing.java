package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * J03-04 · Wrappers, autoboxing e o cache de {@code Integer}.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 127 == 127 -&gt; true
 * 128 == 128 -&gt; false
 * 128 equals 128 -&gt; true
 * unboxing de null -&gt; NullPointerException
 * Boolean.valueOf cacheia -&gt; true
 * sum com Long -&gt; 499500
 * </pre>
 *
 * <p>Variações: rode com {@code -XX:AutoBoxCacheMax=1000} e veja {@code j1 == j2} virar {@code true}.
 */
public class WrappersBoxing {
    @SuppressWarnings("NumberEquality")
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Integer i1 = 127, i2 = 127, j1 = 128, j2 = 128;
        out.add("127 == 127 -> " + (i1 == i2));
        out.add("128 == 128 -> " + (j1 == j2));
        out.add("128 equals 128 -> " + j1.equals(j2));
        Integer n = null;
        try {
            int z = n;
            out.add("z=" + z);
        } catch (NullPointerException e) {
            out.add("unboxing de null -> NullPointerException");
        }
        out.add("Boolean.valueOf cacheia -> " + (Boolean.valueOf("true") == Boolean.valueOf(true)));
        Long sum = 0L;                                   // boxing a cada iteração: um objeto por soma
        for (long k = 0; k < 1000; k++) sum += k;
        out.add("sum com Long -> " + sum);
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
