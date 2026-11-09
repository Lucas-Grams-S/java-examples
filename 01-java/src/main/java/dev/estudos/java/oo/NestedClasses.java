package dev.estudos.java.oo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J04-06 · Classes internas, aninhadas, locais e anônimas.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.6.
 *
 * <p>O campo sintético {@code this$0} é mostrado via reflexão (equivale ao {@code javap -p}).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Inner.y (usa x do Outer) -&gt; 2
 * campos de Inner -&gt; [this$0 (sintético), y]
 * campos de Nested -&gt; []
 * Nested é static -&gt; true
 * anônima vê local=5 e x=1
 * classe local
 * lambda é classe anônima? -&gt; false
 * </pre>
 *
 * <p>Variações: declare {@code Inner} como {@code static} e veja {@code this$0} sumir (e o acesso a {@code x} deixar de compilar).
 */
public class NestedClasses {
    static class Outer {
        int x = 1;
        class Inner { int y = x + 1; }
        static class Nested {}

        Runnable r(List<String> sink) {
            int local = 5;
            return new Runnable() { public void run() { sink.add("anônima vê local=" + local + " e x=" + x); } };
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Outer o = new Outer();
        Outer.Inner in = o.new Inner();
        out.add("Inner.y (usa x do Outer) -> " + in.y);
        new Outer.Nested();
        var fields = Arrays.stream(Outer.Inner.class.getDeclaredFields())
                .map(f -> f.getName() + (f.isSynthetic() ? " (sintético)" : "")).sorted().toList();
        out.add("campos de Inner -> " + fields);
        out.add("campos de Nested -> " + Arrays.toString(Outer.Nested.class.getDeclaredFields()));
        out.add("Nested é static -> " + Modifier.isStatic(Outer.Nested.class.getModifiers()));
        o.r(out).run();

        class Local { String hi() { return "classe local"; } }
        out.add(new Local().hi());

        Runnable lambda = () -> {};
        out.add("lambda é classe anônima? -> " + lambda.getClass().isAnonymousClass());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
