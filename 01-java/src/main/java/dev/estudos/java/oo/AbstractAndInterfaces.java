package dev.estudos.java.oo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * J04-05 · Abstração: classes abstratas e interfaces ({@code default}, {@code static}, {@code private}, diamante).
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Discount.none() -&gt; 10
 * default usa método private -&gt; 3.33
 * name() em classe -&gt; Half
 * name() em lambda contém 'Lambda' -&gt; true
 * diamante resolvido com A.super/B.super -&gt; AB
 * interface mais específica vence -&gt; Specific
 * classe abstrata -&gt; quadrado área=4.0
 * </pre>
 *
 * <p>Variações: remova o método {@code hi()} de {@code C} e leia o erro do compilador sobre o conflito.
 */
public class AbstractAndInterfaces {
    interface Discount {
        BigDecimal apply(BigDecimal total);
        default String name() { return getClass().getSimpleName().isEmpty() ? "lambda/anônima" : getClass().getSimpleName(); }
        static Discount none() { return total -> total; }
        private BigDecimal round(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
        default BigDecimal applyRounded(BigDecimal total) { return round(apply(total)); }
    }

    static class Half implements Discount {
        @Override public BigDecimal apply(BigDecimal total) { return total.divide(BigDecimal.valueOf(3), 5, RoundingMode.DOWN); }
    }

    interface A { default String hi() { return "A"; } }
    interface B { default String hi() { return "B"; } }
    static class C implements A, B {
        @Override public String hi() { return A.super.hi() + B.super.hi(); }
    }

    interface Base { default String who() { return "Base"; } }
    interface Specific extends Base { @Override default String who() { return "Specific"; } }
    static class Impl implements Base, Specific {}

    static abstract class Shape {
        private final String label;
        Shape(String label) { this.label = label; }
        abstract double area();
        String show() { return label + " área=" + area(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var ten = new BigDecimal("10");
        out.add("Discount.none() -> " + Discount.none().apply(ten));
        out.add("default usa método private -> " + new Half().applyRounded(ten));
        out.add("name() em classe -> " + new Half().name());
        out.add("name() em lambda contém 'Lambda' -> " + Discount.none().name().contains("Lambda"));
        out.add("diamante resolvido com A.super/B.super -> " + new C().hi());
        out.add("interface mais específica vence -> " + new Impl().who());
        out.add("classe abstrata -> " + new Shape("quadrado") { double area() { return 4.0; } }.show());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
