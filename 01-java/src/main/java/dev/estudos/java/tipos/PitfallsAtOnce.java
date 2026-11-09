package dev.estudos.java.tipos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * J03-13 · Armadilhas de uma só vez (programa de teste).
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * true false true
 * NPE no unboxing
 * true
 * BigDecimal 2.0 equals 2.00 -&gt; false
 * BigDecimal 2.0 compareTo 2.00 -&gt; 0
 * </pre>
 */
public class PitfallsAtOnce {
    @SuppressWarnings("NumberEquality")
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Integer a = 127, b = 127, c = 128, d = 128;
        out.add((a == b) + " " + (c == d) + " " + c.equals(d));
        Integer n = null;
        try { int z = n; out.add("z=" + z); } catch (NullPointerException e) { out.add("NPE no unboxing"); }
        out.add("" + new OrdersDomain.Money(new BigDecimal("1"), "BRL").equals(new OrdersDomain.Money(new BigDecimal("1"), "BRL")));
        out.add("BigDecimal 2.0 equals 2.00 -> " + new BigDecimal("2.0").equals(new BigDecimal("2.00")));
        out.add("BigDecimal 2.0 compareTo 2.00 -> " + new BigDecimal("2.0").compareTo(new BigDecimal("2.00")));
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
